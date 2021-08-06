package com.example.demo1_opengl.holder

import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.opengl.GLES20
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import java.util.*


/**
 * Created by zyy on 2021/7/12
 *
 * 控制类
 * camera -> surfaceTexture -> oesTexture -> 屏幕
 */
class CameraPresenter() : Camera.PreviewCallback{




    private var cameraId : Int =getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
    private var camera : Camera = Camera.open(cameraId)
    private lateinit var parameters : Camera.Parameters
    private var orientation : Int = 0
    private  var mPreviewCallback : Camera.PreviewCallback? = null
    private lateinit var surfaceTexture: SurfaceTexture

    private lateinit var buffer : ByteArray

    var width = 720
    var height = 1280

    init {



    }

    private fun getCameraId(faceOrBack : Int) : Int{
        val numbers = Camera.getNumberOfCameras()
        for (i in 0 until numbers){
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i,info)
            if(info.facing == faceOrBack)
                return i
        }
        return  -1
    }




    fun stopPreview(){
        camera.setPreviewCallback(null)
        camera.stopPreview()
        camera.release()
    }

    fun startPreview(surfaceTexture: SurfaceTexture){
        this.surfaceTexture = surfaceTexture
        initParameters()
        setPreviewSize()
        camera.setPreviewTexture(surfaceTexture)
        camera.startPreview()
    }

    fun setPreviewCallback(previewCallback: Camera.PreviewCallback){
        mPreviewCallback = previewCallback
    }

    private fun initParameters(){
        try{
            parameters = camera.parameters
            //预览格式
            parameters.previewFormat = ImageFormat.NV21

            //对焦模式
            val supportedFocusModes = parameters.supportedFocusModes
            if (supportedFocusModes != null && supportedFocusModes.size > 0) {
                when {
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    }
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    }
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                    }
                }
            }
            camera.parameters = parameters
        }catch (e:Exception){
            e.printStackTrace()
        }
    }






    /**
     * 设置预览大小
     */
    private fun setPreviewSize(){


        var previewSize : Camera.Size = parameters.previewSize
        val supportedPreviewSizes = parameters.supportedPreviewSizes
        if (supportedPreviewSizes != null && supportedPreviewSizes.size > 0) {
            previewSize =
                getBestSupportedSize(supportedPreviewSizes, Point(width,height))!!
        }
        parameters.setPreviewSize(previewSize.width, previewSize.height)

        camera.parameters = parameters
        buffer = ByteArray(previewSize.width * previewSize.height * 3 / 2)

        //数据缓存区
        camera.addCallbackBuffer(buffer)
        camera.setPreviewCallbackWithBuffer(this)
    }

    /**
     * 最佳大小
     */
    private fun getBestSupportedSize(
        sizes: List<Camera.Size>,
        previewViewSize: Point?,
    ): Camera.Size? {
        var sizes: List<Camera.Size>? = sizes
        if (sizes == null || sizes.size == 0) {
            return camera.getParameters().getPreviewSize()
        }
        val tempSizes = sizes.toTypedArray()
        Arrays.sort(tempSizes
        ) { o1, o2 ->
            if (o1.width > o2.width) {
                -1
            } else if (o1.width == o2.width) {
                if (o1.height > o2.height) -1 else 1
            } else {
                1
            }
        }
        sizes = Arrays.asList(*tempSizes)
        var bestSize = sizes[0]
        var previewViewRatio: Float
        previewViewRatio = if (previewViewSize != null) {
            previewViewSize.x.toFloat() / previewViewSize.y.toFloat()
        } else {
            bestSize.width.toFloat() / bestSize.height.toFloat()
        }
        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio
        }
        val isNormalRotate = orientation % 180 == 0
        for (s in sizes) {
            if (width == s.width && height == s.height) {
                return s
            }
            if (isNormalRotate) {
                if (Math.abs(s.height / s.width.toFloat() - previewViewRatio) < Math.abs(bestSize.height / bestSize.width.toFloat() - previewViewRatio)) {
                    bestSize = s
                }
            } else {
                if (Math.abs(s.width / s.height.toFloat() - previewViewRatio) < Math.abs(bestSize.width / bestSize.height.toFloat() - previewViewRatio)) {
                    bestSize = s
                }
            }
        }
        return bestSize
    }


    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        //数据是倒的
        mPreviewCallback?.onPreviewFrame(data, camera)
        camera?.addCallbackBuffer(buffer)


    }


}