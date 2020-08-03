package com.lk.tnndemo

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class DetectFaceAnalyzer
    : ImageAnalysis.Analyzer {
    companion object{
        const val TAG = "DetectFaceAnalyzer"
    }

    override fun analyze(imageProxy: ImageProxy) {
        Log.d(TAG, "analyze: image-size: ${imageProxy.width}x${imageProxy.height}")

        imageProxy.close()
    }

}