package com.example.demo1_opengl.filter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.demo1_opengl.utils.BufferUtil
import com.example.demo1_opengl.utils.GLUtil
/**
 * Created by zyy on 2021/7/12
 *
 * oes预览相机 并添加水印
 */
class Drawer (context: Context){
    private val TAG : String = "Drawer"
    //顶点坐标
    val VERTEX_COORDS = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1f,1f,
        1.0f, 1.0f,
    )

    //纹理坐标

    val TEXTURE_COORDS = floatArrayOf(
        1.0f, 0.0f,
        1.0f, 1f,
        0.0f, 0.0f,
        0.0f, 1.0f
    )


/*
    val TEXTURE_COORDS = floatArrayOf(
        0.0f,1.0f,
        0.0f,0.0f,
        1.0f,1.0f,
        1.0f,0.0f
    )


 */


    //水印坐标
    val WATER_COORDS = floatArrayOf(
        -0.9f,-0.9f,
        0f,-0.9f,
        -0.9f,-0.85f,
        0f,-0.85f
    )
    val WATER_TEXTURE_COORDS = floatArrayOf(
        0f,1f,
        1f,1f,
        0f,0f,
        1f,0f
    )

    var matrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f)


    //水印
    private var bitmap : Bitmap
    private var waterTextureId : Int = 0

    private var mVertexBuffer = BufferUtil.toFloatBuffer(VERTEX_COORDS)
    private var mTextureBuffer = BufferUtil.toFloatBuffer(TEXTURE_COORDS)
    private var mWaterBuffer = BufferUtil.toFloatBuffer(WATER_COORDS)
    private var mWaterTextureBuffer = BufferUtil.toFloatBuffer(WATER_TEXTURE_COORDS)


    private var vertexShaderCode : String = GLUtil.readRawShaderCode(context,"shader/oes_vertex_shader.glsl")
    private var fragmentShaderCode : String = GLUtil.readRawShaderCode(context, "shader/oes_fragment_shader.glsl")
    private var twoFragmentShaderCode : String = GLUtil.readRawShaderCode(context,"shader/base_fragment_shader.glsl")
    private var twoVertexShaderCode : String = GLUtil.readRawShaderCode(context,"shader/base_vertex_shader.glsl")

    var vertexShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_VERTEX_SHADER,vertexShaderCode)
    var fragmentShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)
    var mProgramId : Int = GLUtil.linkProgram(vertexShaderId,fragmentShaderId)

    var tVertexShaderId : Int =  GLUtil.compileShaderCode(GLES20.GL_VERTEX_SHADER,twoVertexShaderCode)
    var tFragmentShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_FRAGMENT_SHADER,twoFragmentShaderCode)
    var mTwoProgramId : Int = GLUtil.linkProgram(tVertexShaderId,tFragmentShaderId)


    //句柄
    var vPosition : Int = 0
    var vCoord : Int = 0
    var vMatrix : Int = 0
    var vTexture : Int = 0

    var tPosition : Int = 0
    var tCoord : Int = 0
    var tTexture : Int = 0
    var tMatrix : Int = 0
    init {



        bitmap = GLUtil.createTextImage("水印~~！！~~",10.0f,"#fff000","#00000000",0.0f)
        createWaterTextureId()


        vPosition = GLES20.glGetAttribLocation(mProgramId,"vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgramId,"vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgramId,"vMatrix");
        vTexture = GLES20.glGetUniformLocation(mProgramId,"vTexture");

        //tMatrix = GLES20.glGetUniformLocation(mProgramId,"u_matrix")
        tPosition = GLES20.glGetAttribLocation(mTwoProgramId,"vPosition");
        tCoord = GLES20.glGetAttribLocation(mTwoProgramId,"vCoord");
        //tMatrix = GLES20.glGetUniformLocation(mTwoProgramId,"vMatrix");
        tTexture = GLES20.glGetUniformLocation(mTwoProgramId,"vTexture");


    }

    /**
     * 创建水印纹理
     */
    private fun createWaterTextureId(){

        waterTextureId = GLUtil.create2DTexture()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,waterTextureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun draw(mTexture :Int ,mtx : FloatArray){
        GLES20.glUseProgram(mProgramId)

        //mVertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)

        //mTextureBuffer.position(0)
        GLES20.glEnableVertexAttribArray(vCoord)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)

        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_SAMPLER_EXTERNAL_OES,mTexture);
        GLES20.glUniform1i(vTexture,0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        drawWater()
    }


    private fun drawWater(){


        GLES20.glUseProgram(mTwoProgramId)

        //水印的位置
        mWaterBuffer.position(0)
        GLES20.glEnableVertexAttribArray(tPosition)
        GLES20.glVertexAttribPointer(tPosition,2,GLES20.GL_FLOAT,false,0,mWaterBuffer)

        mWaterTextureBuffer.position(0)
        GLES20.glEnableVertexAttribArray(tCoord)
        GLES20.glVertexAttribPointer(tCoord, 2, GLES20.GL_FLOAT, false, 0, mWaterTextureBuffer)

        GLES20.glUniformMatrix4fv(tMatrix,1,false,matrix,0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,waterTextureId)
        GLES20.glUniform1i(tTexture,0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)


    }
}