package com.lk.tnndemo

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class DetectFaceAnalyzer(private val modelPath: String, private val analyzerHost: FaceAnalyzerHost)
    : ImageAnalysis.Analyzer {
    companion object{
        const val TAG = "DetectFaceAnalyzer"
        const val COMPUTE = 0 // 0 = CPU, 1 = GPU
    }

    private var ultraFace: UltraFace? = null
    private val isDetecting = AtomicBoolean(false)


    override fun analyze(imageProxy: ImageProxy) {
        Log.d(TAG, "analyze: image-size: ${imageProxy.width}x${imageProxy.height}")
        if (ultraFace == null)
            ultraFace = UltraFace(modelPath, imageProxy.width, imageProxy.height, COMPUTE)

        if (isDetecting.compareAndSet(false, true)){
            val rotation = getImageRotation(imageProxy.imageInfo.rotationDegrees,
                analyzerHost.getScreenOrientation())
            val yuvBytes = getYuvBytes(imageProxy)
            
            val facesInfo = ultraFace?.detect(yuvBytes, imageProxy.width,
                imageProxy.height, rotation)

            facesInfo?.let { faces->
                Log.d(TAG, "startDetection: result-size = ${faces.size}")
            }

            isDetecting.set(false)
        }
        imageProxy.close()
    }

    private fun getImageRotation(sensor: Int, screen: Int): Int{
        var rotation = (sensor - screen) % 360
        if (rotation < 0)
            rotation = 360 - rotation
        return rotation
    }

    private fun getYuvBytes(imageProxy: ImageProxy): ByteArray {
        val bufferSize = (imageProxy.width * imageProxy.height * (3f/2f)).toInt()
        val planes = imageProxy.planes
        val y = planes[0]
        val u = planes[1]

        Log.d(TAG, "getYuvBytes: planes-size: y = ${y.buffer.remaining()}, u = ${u.buffer.remaining()}")
        val yuvBuffer = ByteBuffer.allocate(bufferSize).put(y.buffer).put(u.buffer)
        return yuvBuffer.array()
    }

    interface FaceAnalyzerHost{
        fun getScreenOrientation(): Int
    }

}