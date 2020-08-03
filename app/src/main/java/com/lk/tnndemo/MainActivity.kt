package com.lk.tnndemo

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MainActivity"
    }
    private var testBitmap: Bitmap? = null
    private lateinit var ultraFace: UltraFace
    private val NET_H_INPUT = 240
    private val NET_W_INPUT = 320

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("demo")
        val modelPath = initModel()
        initViews()

        ultraFace = UltraFace(modelPath, NET_W_INPUT, NET_H_INPUT, 0)
    }

    private fun initViews() {
        try {
            testBitmap = readBitmapFromFile(assets, "test.jpg")
            ivTestImage.setImageBitmap(testBitmap)
        }catch (e: IOException){
            Toast.makeText(this, "error loading image", Toast.LENGTH_LONG).show()
        }
        btnDetect.setOnClickListener { startDetection() }
    }

    private fun startDetection() {
        testBitmap?.let {
            val scaleBitmap = Bitmap.createScaledBitmap(it, NET_W_INPUT,
                NET_H_INPUT, true)
            val facesInfo = ultraFace.detect(scaleBitmap)
            facesInfo?.let { faces->
                Log.d(TAG, "startDetection: result-size = ${faces.size}")
            }

        }
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