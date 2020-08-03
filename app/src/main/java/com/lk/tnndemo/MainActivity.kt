package com.lk.tnndemo

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var testBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("demo")
        initModel()
        initViews()
    }

    private fun initViews() {
        try {
            testBitmap = readBitmapFromFile(assets, "test.jpg")
            ivTestImage.setImageBitmap(testBitmap)
        }catch (e: IOException){
            Toast.makeText(this, "error loading image", Toast.LENGTH_LONG).show()
        }
        btnDetect.setOnClickListener {  }
    }

    @Throws(IOException::class)
    private fun initModel(): String {
        val targetDir: String = filesDir.absolutePath

        //copy detect model to sdcard
        val modelPathsDetector = arrayOf(
            "version-slim-320_simplified.tnnmodel",
            "version-slim-320_simplified.tnnproto"
        )
        for (modelPath in modelPathsDetector) {
            val interModelFilePath = "$targetDir/$modelPath"
           copyAsset(assets, modelPath, interModelFilePath)
        }
        return targetDir
    }

}