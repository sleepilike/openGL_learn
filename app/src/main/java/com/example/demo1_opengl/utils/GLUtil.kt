package com.example.demo1_opengl.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * Created by zyy on 2021/7/12
 *
 */
class GLUtil {
    companion object{

        fun createProgram(
            context: Context,
            vertexFile : String,
            fragmentFile : String) :Int{

            var vertexCode = readRawShaderCode(context,vertexFile)
            var fragmentCode = readRawShaderCode(context,fragmentFile)

            var vertexShaderId = compileShaderCode(GLES20.GL_VERTEX_SHADER,vertexCode)
            var fragmentShaderId = compileShaderCode(GLES20.GL_FRAGMENT_SHADER,fragmentCode)

            return linkProgram(vertexShaderId,fragmentShaderId)

        }
        /**
         * 从assert 中读取ShaderCode
         */
        fun readRawShaderCode(context: Context, shaderCodeName:String) : String{
            var body = StringBuilder()
            try {

                context.assets.open(shaderCodeName).also { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).also {
                        var line = it.readLine()
                        while (line != null){
                            body.append(line)
                            body.append("\n")
                            line = it.readLine()
                        }
                    }
                }
            }catch (e : IOException){
                e.printStackTrace()
            }
            //Log.d("  ", "readRawShaderCode: ${body.toString()}")
            return body.toString()
        }

        /**
         * 编译着色器
         * 大致流程如下：
         * 0.根据着色器的类型，创建一个shaderObjectId=>
         * 1.使用GLES20将我们的代码和ID进行绑定=>
         * 2.编译我们绑定的代码=>
         * 3.1查询编译的状态。如果失败的话，就需要释放资源。delete=>
         * 3.2成功返回我们绑定好编译后代码的shaderObjectId
         */
        fun compileShaderCode(type:Int,shaderCode:String) : Int{

           // Log.d("TAG", "compileShaderCode: $shaderCode")
            //得到一个着色器的id 对id进行操作 it
            GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader,shaderCode)
                GLES20.glCompileShader(shader)
            }
            GLES20.glCreateShader(type).also {
                if(it != 0){
                    //0.上传代码
                    GLES20.glShaderSource(it,shaderCode)
                    //1.编译代码 绑定的代码和对应的id进行编译
                    GLES20.glCompileShader(it)

                    //2.查询编译的状态 失败？
                    var status = IntArray(1)
                    //调用getShaderIv ，传入GL_COMPILE_STATUS进行查询
                    GLES20.glGetShaderiv(it, GLES20.GL_COMPILE_STATUS,status,0)
                    //Log.d("TAG", "compileShaderCode: "+status[0]);
                    //等于0 则表示失败
                    if(status[0] == 0){
                        //释放资源 即删除这个引用
                        GLES20.glDeleteShader(it)
                        Log.d("TAG", "compileShaderCode: compile failed")
                        return 0
                    }
                }
                //返回编译器的id
                return it
            }

        }
        fun linkProgram(vertexShaderId : Int,fragmentShaderId : Int) :Int{

            val programId = GLES20.glCreateProgram()

            GLES20.glAttachShader(programId,vertexShaderId)
            checkGlError("glAttachShader");
            GLES20.glAttachShader(programId,fragmentShaderId)
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(programId)

            var status = IntArray(1)
            GLES20.glGetProgramiv(programId,GLES20.GL_LINK_STATUS,status,0)
            if(status[0] == 0){
                GLES20.glDeleteShader(programId)
                Log.d("TAG", "linkProgram: linkFailed")
                return 0
            }

            return programId
        }


        fun createOESTexture() : Int{
            var textureId = IntArray(1)
            GLES20.glGenTextures(1,textureId,0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId[0])

            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            return textureId[0]

        }

        fun create2DTexture() : Int{
            var textureId = IntArray(1)
            GLES20.glGenTextures(1,textureId,0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
            return textureId[0]
        }

        /**
         * 将文字转为图片
         */
        fun createTextImage(
            text: String,
            textSize: Float,
            textColor: String?,
            bgColor: String?,
            padding: Float,
        ): Bitmap {
            val paint = Paint()
            paint.color = Color.parseColor(textColor)
            paint.textSize = textSize
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true

            val width: Float = paint.measureText(text, 0, text.length)
            val top: Float = paint.fontMetrics.top
            val bottom: Float = paint.fontMetrics.bottom
            val bm = Bitmap.createBitmap((width + padding * 2).toInt(),
                (bottom - top + padding * 2).toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            canvas.drawColor(Color.parseColor(bgColor))
            canvas.drawText(text, padding, -top + padding, paint)
            return bm
        }

        fun checkGlError(op: String) {
            val error = GLES20.glGetError()
            Log.d("TAG", "checkGlError: $error")
            if (error != GLES20.GL_NO_ERROR) {
                val msg = op + ": glError 0x" + Integer.toHexString(error)
                Log.d("TAG", "checkGlError: $msg")
                throw RuntimeException(msg)
            }
        }


    }
}