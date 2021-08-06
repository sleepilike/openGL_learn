package com.example.demo1_opengl.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Created by zyy on 2021/7/12
 *
 */
class PermissionUtil {
    companion object{

        /**
         * 拒绝获取权限弹出提示框
         * @param context
         * @param permission
         */
        fun showPermissionSettingDialog(context: Context, permission: String) {
            val msg = "需要" + permission + "权限才能正常运行，请进入设置界面进行授权处理~"
            val builder = AlertDialog.Builder(context)
            builder.setMessage(msg)
                .setPositiveButton("确定") { _, _ -> //前往设置页
                    showSetting(context)
                }
                .setNegativeButton("取消"
                ) { _, _ -> }
                .show()
        }

        fun showSetting(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}