package com.example.demo1_opengl.filter.base

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.demo1_opengl.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/16
 * 最基本的filter 2D
 */
open class DefaultFilter(context: Context) : AbstractFilter(context) {

    open val VERTEX_FILE = "shader/base_vertex_shader.glsl"
    open val FRAGMNET_FILE = "shader/base_fragment_shader.glsl"

    override fun createProgram(context: Context): Int {

        return GLUtil.createProgram(context,VERTEX_FILE,FRAGMNET_FILE)
    }

    override fun getTextureType(): Int {
        return GLES20.GL_TEXTURE_2D
    }

    override fun onDraw(
        positionHandle: Int,
        vertexBuffer: FloatBuffer,
        coordHandle: Int,
        textureBuffer: FloatBuffer,
        mtx: FloatArray,
        textureId: Int,
        size: Int, //一个点几个坐标
        count: Int, //一共几个点
    ) {

        useProgram()
        bindGLSLValues(size,vertexBuffer,textureBuffer,mtx)
        bindTexture(textureId)
        drawArrays(0,count)

        unbindGLSLValues()
        unbindTexture()
        disUseProgram()

    }


    override fun releaseProgram() {
        super.releaseProgram()
    }

}