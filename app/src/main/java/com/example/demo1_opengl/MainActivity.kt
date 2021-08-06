package com.example.demo1_opengl

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.render.CameraRender
import com.example.demo1_opengl.view.CameraSurfaceView

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class MainActivity : BaseActivity(),View.OnClickListener{

    private lateinit var mView : CameraSurfaceView
    private lateinit var mButton : Button
    private lateinit var mTakeButton: Button
    private lateinit var mGoButton : Button
    private lateinit var mImageView : ImageView
    private var mType : Boolean = true
    private var isStart : Boolean = false

    private var handler : Handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> {
                    Log.d("TAG,","handleMessage: 111")
                    mImageView.setImageBitmap(msg.obj as Bitmap)
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //mLayout = findViewById(R.id.relative_layout)
        if (supportActionBar != null){
            supportActionBar?.hide()
        }

        requestPermission("请给予相机、存储权限，以便app正常工作",
            null,
            *arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))

       // mView = CameraSurfaceView(this)
       // mLayout.addView(mView)
        mView = findViewById(R.id.camera_view)



        mButton = findViewById(R.id.cut_bt)
        mButton.setOnClickListener (this)

        mTakeButton = findViewById(R.id.put_bt)
        mTakeButton.setOnClickListener(this)

        mGoButton = findViewById(R.id.goRecoding_bt)
        mGoButton.setOnClickListener(this)

        mImageView = findViewById(R.id.photo_iv)


        /**
         * 录制完成
         */


    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.cut_bt -> {
                mType = !mType
                mView.change(mType)
            }
            R.id.put_bt ->{
                mView.take(true)
                mView.mRender.setOnListener(object : CameraRender.MInterface{
                    override fun take(bitmap: Bitmap) {
                        var message  = Message()
                        message.what = 1
                        message.obj = bitmap
                       handler.sendMessage(message)
                    }
                })
            }
            R.id.goRecoding_bt ->{
                if ( !isStart){
                    mView.startRecord()
                    isStart = true
                    mGoButton.text = "结束录制"
                }else if (isStart){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mView.stopRecord()
                    }
                    isStart = false
                    mGoButton.text = "开始录制"
                }
            }
        }


    }









}