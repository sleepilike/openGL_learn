package com.example.demo1_opengl.filter.base

import android.opengl.Matrix
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 */
interface IFilter {

    fun getTextureType() : Int
    fun onDraw(positionHandle : Int,vertexBuffer: FloatBuffer, coordHandle : Int,textureBuffer: FloatBuffer,
               mtx : FloatArray, textureId :Int,size : Int,count : Int)

    fun releaseProgram()

}