package com.example.demo1_opengl.utils

import android.opengl.Matrix

/**
 * Created by zyy on 2021/7/19
 *
 */
class MatrixUtil {
    companion object{

        fun flip(m: FloatArray?, x: Boolean, y: Boolean): FloatArray? {

            if (m != null) {
                for ((index,e) in m.withIndex()){
                    println("下标111=$index----元素=$e")
                }
            }
            if (x || y) {
                Matrix.scaleM(m, 0,
                    1f , -1f, 1f)
            }
            if (m != null) {
                for ((index,e) in m.withIndex()){
                    println("下标=$index----元素=$e")
                }
            }
            return m
        }
    }
}