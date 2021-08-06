package com.example.demo1_opengl.filter

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.demo1_opengl.utils.BufferUtil
import com.example.demo1_opengl.utils.GLUtil
import com.example.demo1_opengl.utils.MatrixUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/15
 *
 * oes -> fbo -> 2d ->屏幕
 */
class FBODrawer (context: Context){

    var isTaking : Boolean = false

    var type : Boolean = true;
    //true代表crop false代表inside
    var screenHeight : Int = 2190
    var screenWidth : Int = 1080
    var picHeight : Int = 2190
    var picWidth : Int = 1080
    //顶点坐标
    var VERTEX_COORDS = FloatArray(8)

    //纹理坐标
    var TEXTURE_COORDS = FloatArray(8)

    var matrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f)

    private lateinit var mVertexBuffer : FloatBuffer
    private lateinit var mTextureBuffer : FloatBuffer

    private var fragmentShaderCode : String = GLUtil.readRawShaderCode(context,"shader/base_fragment_shader.glsl")
    private var vertexShaderCode : String = GLUtil.readRawShaderCode(context,"shader/base_vertex_shader.glsl")

    var vertexShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_VERTEX_SHADER,vertexShaderCode)
    var fragmentShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)
    var mProgramId : Int = GLUtil.linkProgram(vertexShaderId,fragmentShaderId)

    var tPosition : Int = 0
    var tCoord : Int = 0
    var tTexture : Int = 0
    var tMatrix :Int = 0
    init {
        tMatrix = GLES20.glGetUniformLocation(mProgramId,"u_matrix")
        tPosition = GLES20.glGetAttribLocation(mProgramId,"vPosition");
        tCoord = GLES20.glGetAttribLocation(mProgramId,"vCoord");
        tTexture = GLES20.glGetUniformLocation(mProgramId,"vTexture");
    }

    var mTextureId : Int = -1

    fun setTextureId(textureId : Int){
        mTextureId = textureId
    }

    fun setSize(sWidth:Int,sHeight:Int,pWidth:Int,pHeight:Int){
        screenWidth = sWidth
        screenHeight = sHeight
        picWidth = pWidth
        picHeight = pHeight
        computeCropTexture()
    }
    fun  draw(){
        GLES20.glUseProgram(mProgramId)

        if (type)
            computeCropTexture()
        else
            computeInside()

        mVertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(tPosition)
        GLES20.glVertexAttribPointer(tPosition,2,GLES20.GL_FLOAT,false,0,mVertexBuffer)

        mTextureBuffer.position(0)
        GLES20.glEnableVertexAttribArray(tCoord)
        GLES20.glVertexAttribPointer(tCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId)
        GLES20.glUniform1i(tTexture,0)


        //GLES20.glUniformMatrix4fv(tMatrix,1,false,matrix,0)


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
    }
    fun setCutType(type : Boolean){
        this.type = type
    }
    fun setIsTaking(isTaking : Boolean){
        this.isTaking = isTaking
    }
    fun computeCropTexture(){
        //纹理坐标做改变
        //放大  宽度需要裁剪居中

        VERTEX_COORDS = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1f,1f,
            1.0f, 1.0f,
        )

        //纹理坐标

        if(isTaking){
            //左上角
            TEXTURE_COORDS = floatArrayOf(
                0f,1f,
                0.5f,1f,
                0f,0.5f,
                0.5f,0.5f
            )
        }else{

            TEXTURE_COORDS = floatArrayOf(
                0f,0f,
                1f,0f,
                0f,1f,
                1f,1f
            )
        }




        var inputAspect : Float = picWidth.toFloat()/picHeight.toFloat()
        var outAspect : Float = screenWidth.toFloat()/screenHeight.toFloat()

        if(inputAspect < outAspect){
            //高裁剪

            var heightRatio = outAspect/inputAspect
            var newHeiht = picHeight.toFloat() *heightRatio
            var sub = (newHeiht - picHeight.toFloat())/picHeight/2
            TEXTURE_COORDS[1] += sub
            TEXTURE_COORDS[3] += sub
            TEXTURE_COORDS[5] -= sub
            TEXTURE_COORDS[7] -= sub

        }else{

            //宽裁剪
            var widthRatio = inputAspect/outAspect
            var newWidth = picWidth.toFloat() * widthRatio
            var sub = (newWidth - picWidth.toFloat())/picWidth/2
            TEXTURE_COORDS[0] += sub
            TEXTURE_COORDS[2] -= sub
            TEXTURE_COORDS[4] += sub
            TEXTURE_COORDS[6] -= sub
        }
        mVertexBuffer = BufferUtil.toFloatBuffer(VERTEX_COORDS)
        mTextureBuffer = BufferUtil.toFloatBuffer(TEXTURE_COORDS)
    }

    fun computeInside(){

        VERTEX_COORDS = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1f,1f,
            1.0f, 1.0f,
        )

        //纹理坐标
        if(isTaking){
            TEXTURE_COORDS = floatArrayOf(
                0f,1f,
                1f,1f,
                0f,0f,
                1f,0f
            )
        }else{
            TEXTURE_COORDS = floatArrayOf(
                0f,0f,
                1f,0f,
                0f,1f,
                1f,1f
            )
        }

        var inputAspect : Float = picWidth.toFloat()/picHeight.toFloat()
        var outAspect : Float = screenWidth.toFloat()/screenHeight.toFloat()
        if(inputAspect < outAspect){

            //宽 背景色
            var widthRatio  = screenHeight/picHeight
            var newWidth = picWidth.toFloat() * widthRatio
            var sub = (screenWidth.toFloat() - newWidth)/screenWidth/2
            Log.d("TAG", "computeInside: $sub")
            VERTEX_COORDS[0] += sub
            VERTEX_COORDS[2] -= sub
            VERTEX_COORDS[4] += sub
            VERTEX_COORDS[6] -= sub

        }else{

            Log.d("TAG", "computeInside: 1111")

            //高 背景色
            var heightRatio = screenWidth/picWidth
            var newHeight = picHeight.toFloat() * heightRatio
            var sub = (screenHeight.toFloat() - newHeight)/screenHeight/2
            VERTEX_COORDS[1] += sub
            VERTEX_COORDS[3] += sub
            VERTEX_COORDS[5] -= sub
            VERTEX_COORDS[7] -= sub

        }

        mVertexBuffer = BufferUtil.toFloatBuffer(VERTEX_COORDS)
        mTextureBuffer = BufferUtil.toFloatBuffer(TEXTURE_COORDS)

    }

}