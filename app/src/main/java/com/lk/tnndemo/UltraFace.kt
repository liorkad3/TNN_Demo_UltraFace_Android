package com.lk.tnndemo

import android.graphics.Bitmap

class UltraFace(modelPath: String, width: Int, height: Int, computeType: Int) {
    private var netAddress: Long = -1

    init {
        netAddress = init(modelPath, width, height, computeType)
    }

    fun detect(bitmap: Bitmap): Array<FaceInfo>?{
        return detectFromImage(netAddress, bitmap, bitmap.width, bitmap.height)
    }

    external fun init(
        modelPath: String,
        width: Int,
        height: Int,
        computeType: Int
    ): Long

    external fun deinit(netAddress: Long): Int
    external fun detectFromStream(
        yuv420sp: ByteArray,
        width: Int,
        height: Int,
        rotate: Int
    ): Array<FaceInfo>

    external fun detectFromImage(
        netAddress: Long,
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): Array<FaceInfo>?

    class FaceInfo {
        var x1 = 0f
        var y1 = 0f
        var x2 = 0f
        var y2 = 0f
        var score = 0f
        var landmarks: FloatArray = FloatArray(5){0f}
    }
}