package com.example.demo1_opengl

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

open class BaseActivity : AppCompatActivity() {
    private var permissionDes: String? = null
    private var callback: Callback? = null
    fun requestPermission(permissionDes: String?, callback: Callback?, vararg permissions: String) {
        this.permissionDes = permissionDes
        this.callback = callback
        if (checkPermissions(*permissions)) {
            callback?.success()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    fun checkPermissions(vararg permissions: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkselfPermissions(*permissions)
        } else true
    }

    fun checkselfPermissions(vararg permissions: String): Boolean {
        var granted = true
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                granted = false
                break
            }
        }
        return granted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        var granted = true
        for (i in grantResults.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                granted = false
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    showPromptDialog()
                } else {
                    if (callback != null) callback!!.failed()
                }
                break
            }
        }
        if (granted) {
            if (callback != null) callback!!.success()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun showPromptDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限申请")
            .setMessage(permissionDes)
            .setCancelable(false)
            .setPositiveButton("去设置"
            ) { dialog, which -> toAppSetting() }
            .setNegativeButton("取消"
            ) { dialog, which -> if (callback != null) callback!!.failed() }.show()
    }

    fun toAppSetting() {
        var settingIntent: Intent? = null
        if (Build.VERSION.SDK_INT >= 9) {
            settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            settingIntent.data = Uri.fromParts("package", packageName, null)
            settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            settingIntent.action = Intent.ACTION_VIEW
            settingIntent.setClassName("com.android.settings",
                "com.android.settings.InstalledAppDetails")
            settingIntent.putExtra("com.android.settings.ApplicationPkgName", packageName)
            settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(settingIntent)
    }

    interface Callback {
        fun success()
        fun failed()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1109
    }
}
