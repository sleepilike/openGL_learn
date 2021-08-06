package com.example.demo1_opengl.utils

import android.content.Context
import android.widget.Toast

/**
 * Created by zyy on 2021/7/12
 *
 */
class ToastUtil {



    companion object{
        var mToast: Toast? = null
        /**
         * 提示轮子
         */
        fun showShortToast(context: Context?, msg: String?) {
            showToastMessage(context, msg, Toast.LENGTH_SHORT)
        }

        /**
         * 提示
         */
        fun showToastMessage(context: Context?, msg: String?, duration: Int) {
            if (mToast == null) {
                mToast = Toast.makeText(context, msg, duration)
            } else {
                mToast!!.setText(msg)
                mToast!!.duration = duration
            }
            mToast!!.show()
        }
    }
}