package com.example.demo1_opengl.record

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGLContext
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import androidx.annotation.RequiresApi
import java.io.IOException

/**
 * Created by zyy on 2021/8/2
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MediaRecord (context: Context,path:String,eglContext: EGLContext){

    private var mContext: Context = context.applicationContext
    private var mPath : String = path
    private var mWidth : Int = 0
    private var mHeight : Int = 0
    private var mEglContext : EGLContext = eglContext
    private lateinit var mMediaCodec: MediaCodec
    private lateinit var mInputSurface: Surface
    private lateinit var mMediaMuxer : MediaMuxer
    private lateinit var mHandler : Handler
    private lateinit var mEglBase : EGLBase
    private var isStart : Boolean = false
    private var index : Int = 0
    //private var mSpeed : Float = 0.0f


    /**
     * 增加录制完成回调
     */
    private var mListener: OnRecordFinishListener? = null

    fun setOnRecordFinishListener(listener: OnRecordFinishListener?) {
        mListener = listener
    }

    public interface OnRecordFinishListener {
        fun onRecordFinish(path: String?)
    }

    init {

    }

    fun setSize(width:Int,height:Int){
        this.mWidth = width
        this.mHeight = height
    }
    /**
     * 开始录制
     */
    @Throws(IOException::class)
    fun start(){
        //mSpeed = speed

        //配置编码器
        initEncoder()

        //配置egl环境
        var handlerThread  = HandlerThread("VideoCodec")
        handlerThread.start()
        var looper : Looper = handlerThread.looper

        //用于其他线程 通知子线程
        mHandler = Handler(looper)

        mHandler.post { //创建我们的子线程，用于把预览的图像存储到虚拟Diaplay中去。
            mEglBase = EGLBase(mContext, mWidth, mHeight, mInputSurface, mEglContext)
            //启动编码器
            mMediaCodec.start()
            isStart = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun encoderFrame(textureId :Int, timestamp : Long){
        if(!isStart)
            return
        mHandler.post { //把图像画到虚拟屏幕
            mEglBase.draw(textureId, timestamp)
            //从编码器的输出缓冲区获取编码后的数据就ok了
            getCodec(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stop() {
        isStart = false
        mHandler.post {
            getCodec(true)
            mMediaCodec.stop()
            mMediaCodec.release()
            //mMediaCodec = null
            mMediaMuxer.stop()
            mMediaMuxer.release()
           // mMediaMuxer = null
            mEglBase.release()
           // mEglBase = null
            //mInputSurface = null
            mHandler.looper.quitSafely()
          //  mHandler = null

            //录制完成，通过回调借口回调出去 并把录制的视频地址传出去
            mListener?.onRecordFinish(mPath)
        }
    }


    private fun initEncoder(){

        /**
         * 配置MediaCodec 编码器
         */
        //视频格式
        // 类型（avc高级编码 h264） 编码出的宽、高
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight)
        //参数配置
        // 1500kbs码率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1500000)
        //帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        //关键帧间隔
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 20)
        //颜色格式（RGB\YUV）
        //从surface当获取
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        //编码器
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        //将参数配置给编码器
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)



        //交给虚拟屏幕 通过opengl 将预览的纹理 绘制到这一个虚拟屏幕中
        //这样MediaCodec 就会自动编码 inputSurface 中的图像
        mInputSurface = mMediaCodec.createInputSurface()


        //  H.264
        // 播放：
        //  MP4 -> 解复用 (解封装) -> 解码 -> 绘制
        //封装器 复用器
        // 一个 mp4 的封装器 将h.264 通过它写出到文件就可以了
        mMediaMuxer = MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    /**
     * 获取编码后 的数据
     *
     * @param endOfStream 标记是否结束录制
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCodec(endOfStream: Boolean) {

        //结束录制 给mediacodec一个标记
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream()
        }
        //输出缓冲区
        val bufferInfo = MediaCodec.BufferInfo()
        // 希望将已经编码完的数据都 获取到 然后写出到mp4文件
        while (true) {
            //等待10 ms
            val status = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            //让我们重试  1、需要更多数据  2、可能还没编码为完（需要更多时间）
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // 如果是停止 我继续循环
                // 继续循环 就表示不会接收到新的等待编码的图像
                // 相当于保证mediacodec中所有的待编码的数据都编码完成了，不断地重试 取出编码器中的编码好的数据
                // 标记不是停止 ，我们退出 ，下一轮接收到更多数据再来取输出编码后的数据
                if (!endOfStream) {
                    //不写这个 会卡太久了，没有必要 你还是在继续录制的，还能调用这个方法的！
                    break
                }
                //否则继续
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //开始编码 就会调用一次
                val outputFormat = mMediaCodec.outputFormat
                //配置封装器
                // 增加一路指定格式的媒体流 视频
                index = mMediaMuxer.addTrack(outputFormat)
                mMediaMuxer.start()
            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //忽略
            } else {
                //成功 取出一个有效的输出
                val outputBuffer = mMediaCodec.getOutputBuffer(status)
                //如果获取的ByteBuffer 是配置信息 ,不需要写出到mp4
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }
                if (bufferInfo.size != 0) {
                   // bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs / mSpeed).toLong()
                    //写到mp4
                    //根据偏移定位
                    outputBuffer!!.position(bufferInfo.offset)
                    //ByteBuffer 可读写总长度
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    //写出
                    mMediaMuxer.writeSampleData(index, outputBuffer, bufferInfo)
                }
                //输出缓冲区 我们就使用完了，可以回收了，让mediacodec继续使用
                mMediaCodec.releaseOutputBuffer(status, false)
                //结束
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }
}