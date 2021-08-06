package com.example.demo1_opengl.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by zyy on 2021/7/12
 *
 */
class BufferUtil {
    companion object{

        fun toFloatBuffer(array:FloatArray) : FloatBuffer {
            return ByteBuffer.allocateDirect(array.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(array)
                    // 将缓冲区的指针移动到头部，保证数据是从最开始处读取
                    position(0)
                }
            }
        }

        fun toShortBuffer(array:ShortArray) : ShortBuffer{
            return ByteBuffer.allocateDirect(array.size * 2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(array)
                    // 将缓冲区的指针移动到头部，保证数据是从最开始处读取
                    position(0)
                }
            }
        }
    }
}