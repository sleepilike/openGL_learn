package com.example.demo1_opengl.filter.base

import android.content.Context
import android.opengl.GLES20
import com.example.demo1_opengl.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 */
abstract class AbstractFilter(context: Context) : IFilter{
    var context : Context = context
    var mProgramId : Int = createProgram(context)



    abstract fun createProgram(context: Context) : Int



    var vPosition : Int = 0
    var vCoord : Int = 0
    var vTexture : Int = 0
    var vMatrix : Int = 0
    fun getGLSLHandle(){
        vPosition = GLES20.glGetAttribLocation(mProgramId,"vPosition")
        vCoord = GLES20.glGetAttribLocation(mProgramId,"vCoord")
        vTexture = GLES20.glGetUniformLocation(mProgramId,"vTexture")
        vMatrix = GLES20.glGetUniformLocation(mProgramId,"vMatrix")
    }

    fun useProgram(){
        GLES20.glUseProgram(mProgramId)
    }

    fun bindGLSLValues(
        size : Int,
        vertexBuffer : FloatBuffer,
        textureBuffer : FloatBuffer,
        matrix : FloatArray,
        ){
        bindGLSLValues(size,vertexBuffer,textureBuffer)
        GLES20.glUniformMatrix4fv(vMatrix,1,false,matrix, 0)
    }
    fun bindGLSLValues(
        size : Int,
        vertexBuffer : FloatBuffer,
        textureBuffer : FloatBuffer,
    ){

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition,size,GLES20.GL_FLOAT,false,0,vertexBuffer)

        GLES20.glEnableVertexAttribArray(vCoord)
        GLES20.glVertexAttribPointer(vCoord,size,GLES20.GL_FLOAT,false,0,textureBuffer)

    }


    var defaultTexture :Int = -1
   fun bindTexture(textureId : Int){

       GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
       GLES20.glBindTexture(getTextureType(),textureId)
       GLES20.glUniform1i(textureId,0)
   }

    fun drawArrays(first : Int,count : Int){
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,first,count)
    }

    fun unbindGLSLValues(){
        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(vTexture)
    }

    fun unbindTexture(){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
    }

    fun deleteTexture(){
        if(defaultTexture != -1){
            GLES20.glDeleteTextures(1, intArrayOf(defaultTexture),0)
            defaultTexture = -1
        }
    }

    fun disUseProgram(){
        GLES20.glUseProgram(0)
    }

    override fun releaseProgram() {
        deleteTexture()
        GLES20.glDeleteProgram(mProgramId)
        mProgramId = -1
    }





}