package com.example.demo1_opengl.filter

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.demo1_opengl.filter.base.AbstractFilter
import com.example.demo1_opengl.filter.base.DefaultFilter
import com.example.demo1_opengl.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 * 相机 外部纹理 oes 实现预览
 */
class CameraOESFilter(context: Context) : DefaultFilter(context) {
    override val VERTEX_FILE = "shader/oes_vertex_shader.glsl"
    override val FRAGMNET_FILE = "shader/oes_fragment_shader.glsl"


    override fun createProgram(context: Context): Int {
        return GLUtil.createProgram(context,VERTEX_FILE,FRAGMNET_FILE)
    }

    override fun getTextureType(): Int {
        return GLES11Ext.GL_SAMPLER_EXTERNAL_OES
    }

    override fun onDraw(
        positionHandle: Int,
        vertexBuffer: FloatBuffer,
        coordHandle: Int,
        textureBuffer: FloatBuffer,
        mtx: FloatArray,
        textureId: Int,
        size: Int,
        count: Int
    ) {
        super.onDraw(positionHandle, vertexBuffer, coordHandle, textureBuffer,  mtx, textureId, size, count)
    }


    override fun releaseProgram() {
        super.releaseProgram()
    }
}