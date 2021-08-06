package com.example.demo1_opengl.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.record.MediaRecord
import com.example.demo1_opengl.render.CameraRender


/**
 * Created by zyy on 2021/7/12
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class CameraSurfaceView : GLSurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)





    var mRender : CameraRender

   // var imageView : ImageView

    init {
        setEGLContextClientVersion(2)
        mRender = CameraRender(this)
        setRenderer(mRender)
        renderMode = RENDERMODE_WHEN_DIRTY;

       // imageView = findViewById(R.id.photo_img)
    }

    fun change(boolean: Boolean){

        mRender.changeType(boolean)

    }
    fun take(boolean: Boolean) {
        Log.d("TAG", "take: aaaa")
        mRender.setTaking(boolean)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun startRecord(){
        mRender.startRecord()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopRecord(){
        mRender.stopRecord()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun setOnRecordFinishListener(listener: MediaRecord.OnRecordFinishListener) {
        mRender.setOnRecordFinishListener(listener)
    }




}