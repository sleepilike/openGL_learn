package com.example.demo1_opengl.filter

import android.content.Context
import com.example.demo1_opengl.filter.base.DefaultFilter

/**
 * Created by zyy on 2021/7/23
 *
 * 图片裁剪
 * center_crop && center_inside
 */

enum class CutType{
    CENTER_CROP,
    CENTER_INSIDE
}
class CutFilter (context: Context): DefaultFilter(context) {

    private lateinit var type : CutType

    private var picWidth : Int = 0
    private var picHeight : Int = 0
    private var screenWidth : Int = 0
    private var screenHeight : Int = 0

    fun setType(type : CutType){
        this.type = type
    }
    fun getType() : CutType{
        return this.type
    }

    fun setSize(picWidth : Int,picHeight : Int,screenWidth : Int,screenHeight : Int){
        this.picWidth = picWidth
        this.picHeight = picHeight
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }
    fun computeCoord(coords : FloatArray){

        var inputAspect : Float = picWidth.toFloat()/picHeight.toFloat()
        var outAspect : Float = screenWidth.toFloat()/screenHeight.toFloat()

        when(type){
            /**
             * 传递的是纹理坐标 对纹理坐标进行裁剪
             * 居中裁剪 铺满
             */
            CutType.CENTER_CROP -> {
                if (inputAspect < outAspect){
                    //高裁剪 修改y坐标
                    var heightRatio  = outAspect/inputAspect
                    var newHeight = picHeight.toFloat() * heightRatio
                    var cut = (newHeight - picHeight.toFloat())/picHeight/2
                    coords[1] += cut
                    coords[3] += cut
                    coords[5] -= cut
                    coords[7] -= cut
                }else{
                    //宽裁剪 修改x坐标
                    var widthRatio = inputAspect / outAspect
                    var newWidth = picWidth.toFloat() * widthRatio
                    var cut = (newWidth - picWidth.toFloat())/picWidth/2
                    coords[0] += cut
                    coords[2] -= cut
                    coords[4] += cut
                    coords[6] -= cut
                }
            }
            /**
             * 传递的是顶点坐标
             * 居中 填满x轴 或 y 轴 不够填充背景色
             */
            CutType.CENTER_INSIDE ->{

                if (inputAspect < outAspect){
                    //高充满 宽为背景色 修改x坐标
                    var widthRatio  = screenHeight/picHeight
                    var newWidth = picWidth.toFloat() * widthRatio
                    var cut = (screenWidth.toFloat() - newWidth)/screenWidth/2
                    coords[0] += cut
                    coords[2] -= cut
                    coords[4] += cut
                    coords[6] -= cut
                }else{
                    //宽充满 高空余为背景色 修改y坐标
                    var heightRatio = screenWidth/picWidth
                    var newHeight = picHeight.toFloat() * heightRatio
                    var cut = (screenHeight.toFloat() - newHeight)/screenHeight/2
                    coords[1] += cut
                    coords[3] += cut
                    coords[5] -= cut
                    coords[7] -= cut
                }
            }
        }
    }


}