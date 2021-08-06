package com.example.demo1_opengl.holder

import android.graphics.ImageFormat
import android.graphics.Point
import android.hardware.Camera
import java.lang.Exception
import java.util.*

/**
 * Created by zyy on 2021/7/23
 *
 */
class CameraPresenter2 : Camera.PreviewCallback{

    private var cameraId : Int = getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
    private var camera : Camera = Camera.open(cameraId)

    private var width : Int = 0
    private var height : Int = 0

    private var orientation : Int = 0

    fun setOrientation(orientation : Int){
        this.orientation = orientation
    }
    fun setSize(width : Int,height : Int){
        this.width = width
        this.height = height
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

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

    private fun initParameters(){
        try {
            var parameters = camera.parameters
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
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun setPreViewSize(){
        var parameters = camera.parameters
        var previewSize : Camera.Size = parameters.previewSize

        val supportedPreviewSizes = parameters.supportedPreviewSizes
        if (supportedPreviewSizes != null && supportedPreviewSizes.size > 0) {
            previewSize =
                getBestSupportedSize(supportedPreviewSizes, Point(width,height))!!
        }
        parameters.setPreviewSize(previewSize.width,previewSize.height)

        camera.parameters = parameters
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


}