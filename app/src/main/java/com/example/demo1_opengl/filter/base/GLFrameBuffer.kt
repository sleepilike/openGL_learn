package com.example.demo1_opengl.filter.base

import android.opengl.GLES20
import android.util.Log
import com.example.demo1_opengl.utils.GLUtil

/**
 * Created by zyy on 2021/7/15
 *
 */
class GLFrameBuffer (){

     val TAG : String = "GLFrameBuffer"
     var mFrameBuffer : Int = -1
     var mRenderBuffer : Int =-1
     var height : Int = 2190
     var width : Int = 1080
     var m2DTextureId : Int = -1



     fun prepare(){
          create2DTexture()
          createAndBindFrameBuffer()
          //createAndBindRenderBuffer()
          attachFrameBuffer()
     }


     private fun create2DTexture () {

          m2DTextureId = GLUtil.create2DTexture()
          GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,m2DTextureId)
          GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
               GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
          GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)

     }

     private fun createAndBindFrameBuffer() {
          var values  = IntArray(2)
          GLES20.glGenFramebuffers(1,values,0)
          mFrameBuffer = values[0]
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mFrameBuffer)
     }

     private fun createAndBindRenderBuffer(){
          var value = IntArray(2)
          GLES20.glGenRenderbuffers(1,value,0)
          mRenderBuffer = value[0]
          GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,mRenderBuffer)
          //为renderBuffer申请空间
          GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,width,height)
     }

     private fun attachFrameBuffer(){
          // 将renderBuffer挂载到frameBuffer的depth attachment 上
         // GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mRenderBuffer);

          // 将text2d挂载到frameBuffer的color attachment上
          GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, m2DTextureId, 0);

          var status :Int = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
          if(status != GLES20.GL_FRAMEBUFFER_COMPLETE)
               Log.d(TAG, "attachFrameBuffer: $status")
            //   throw RuntimeException("Framebuffer not complete, status=" + status);
          GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
     }


}