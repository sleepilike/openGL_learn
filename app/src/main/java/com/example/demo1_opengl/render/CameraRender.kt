package com.example.demo1_opengl.render

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.*
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.filter.Drawer
import com.example.demo1_opengl.filter.FBODrawer
import com.example.demo1_opengl.filter.base.GLFrameBuffer
import com.example.demo1_opengl.holder.CameraPresenter
import com.example.demo1_opengl.record.MediaRecord
import com.example.demo1_opengl.utils.GLUtil
import java.io.*
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by zyy on 2021/7/12
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class CameraRender(glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private var cameraPresenter :CameraPresenter = CameraPresenter()
    private lateinit var mDrawer: Drawer
    private lateinit var mFBODrawer: FBODrawer
    private var mTexture : Int = -1
    private lateinit var mSurfaceTexture: SurfaceTexture
    private var glSurfaceView : GLSurfaceView = glSurfaceView
    private lateinit var glFrameBuffer : GLFrameBuffer
    private lateinit var takeFrameBuffer : GLFrameBuffer

    private lateinit var mMediaRecord : MediaRecord
    //private lateinit var mRecordListener : MediaRecord.OnRecordFinishListener

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun setOnRecordFinishListener(listener: MediaRecord.OnRecordFinishListener) {
        if (null != mMediaRecord) {
            mMediaRecord.setOnRecordFinishListener(listener)
        }
       // mRecordListener = listener
    }

    private var mType : Boolean = true
    //变换矩阵
    private var mtx = FloatArray(16)

    private var isTaking : Boolean = false

    private lateinit var mListener : MInterface
    interface  MInterface{
        fun take(bitmap: Bitmap)
    }
    fun setOnListener(mInterface: MInterface){
        this.mListener = mInterface
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //文字支持透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        mTexture = GLUtil.createOESTexture()
        mSurfaceTexture = SurfaceTexture(mTexture)
        mSurfaceTexture.setOnFrameAvailableListener(OnFrameAvailableListener {
            //触发 GLSurfaceView 的render的 onDrawFrame
            glSurfaceView.requestRender()
        })

        glFrameBuffer = GLFrameBuffer()

        mDrawer = Drawer(glSurfaceView.context)
        mFBODrawer = FBODrawer(glSurfaceView.context)

        takeFrameBuffer = GLFrameBuffer()

        //渲染线程的上下文 作为EGL的sharedContext
        var eglContext : EGLContext = EGL14.eglGetCurrentContext()
        val folderPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/a.mp4"
        mMediaRecord = MediaRecord(glSurfaceView.context,folderPath,eglContext)
//        mMediaRecord.setOnRecordFinishListener(mRecordListener)

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {


        GLES20.glViewport(0,0,width,height )
        cameraPresenter.width = width
        cameraPresenter.height = height

        glFrameBuffer.width = width
        glFrameBuffer.height = 16*width/9
        glFrameBuffer.prepare()

        takeFrameBuffer.width = width;
        takeFrameBuffer.height = height
        takeFrameBuffer.prepare()



        mFBODrawer.setSize(width,height,glFrameBuffer.width,glFrameBuffer.height)

        //开始预览
        cameraPresenter.startPreview(mSurfaceTexture)

        mMediaRecord.setSize(width,height)


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        //将摄像头数据从surfaceTure中取出
        mSurfaceTexture.updateTexImage()
        mSurfaceTexture.getTransformMatrix(mtx)



        //将数据绘制到fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,glFrameBuffer.mFrameBuffer)
        mDrawer.draw(mTexture,mtx )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mFBODrawer.setTextureId(glFrameBuffer.m2DTextureId)



        if(isTaking){
            //渲染到临时framBuffer
            //bindFrameBufferAndTexture()
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,takeFrameBuffer.mFrameBuffer)
            mFBODrawer.setIsTaking(isTaking)
            mFBODrawer.draw()
            sendImage(takeFrameBuffer.width,takeFrameBuffer.height)
           // unBindFrameBuffer()
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            isTaking = false
            mFBODrawer.setIsTaking(isTaking)
        }

        mFBODrawer.draw()

        //glFrameBuffer.m2DTextureId为预览+水印
        //录制
        mMediaRecord.encoderFrame(glFrameBuffer.m2DTextureId,mSurfaceTexture.timestamp)

    }

    fun changeType(boolean: Boolean){
        mType = boolean
        mFBODrawer.setCutType(mType)
    }

    fun sendImage(width : Int,height: Int){
        Log.d("TAG", "sendImage: $width")
        var buffer : ByteBuffer = ByteBuffer.allocateDirect(width * height * 4)
        buffer.position(0)

        GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer)

        savePhoto(buffer,width,height)
    }

    fun savePhoto(buffer: ByteBuffer,width: Int,height: Int) {

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        mListener.take(bitmap)


        /*
        Thread(Runnable {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)

            val folderPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/"
            val folder = File(folderPath)
            if (!folder.exists() && !folder.mkdirs()) {
                Log.e("demos", "图片目录异常")
                return@Runnable
            }
            val filePath = folderPath + System.currentTimeMillis() + ".jpg"
            var bos: BufferedOutputStream? = null
            try {
                val fos = FileOutputStream(filePath)
                bos = BufferedOutputStream(fos)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                if (bos != null) {
                    try {
                        bos.flush()
                        bos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                if (bitmap != null) {
                    bitmap.recycle()
                }
            }
        }).start()


         */


    }




    fun setTaking(isTaking : Boolean){
        this.isTaking = isTaking
        Log.d("TAG", "setTaking: $isTaking")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun startRecord() {
        try {
            mMediaRecord.start()
            Log.d("TAG", "startRecord: 开始录制")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopRecord() {
        mMediaRecord.stop()
        Log.d("TAG", "stopRecord: 结束录制")
    }


}