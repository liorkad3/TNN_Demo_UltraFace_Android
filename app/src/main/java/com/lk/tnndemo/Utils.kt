package com.lk.tnndemo

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

@Throws(IOException::class)
fun copyAsset(assetManager: AssetManager,
               fromAssetPath: String, toPath: String){
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = assetManager.open(fromAssetPath)
        File(toPath).createNewFile()
        outputStream = FileOutputStream(toPath)
        copyFile(inputStream, outputStream)
        outputStream.flush()
    }finally {
        inputStream?.close()
        outputStream?.close()
    }
}

@Throws(IOException::class)
fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
    val buffer = ByteArray(1024)
    var read: Int
    while (inputStream.read(buffer).also { read = it } != -1) {
        outputStream.write(buffer, 0, read)
    }
}
@Throws(IOException::class)
fun readBitmapFromFile(assetManager: AssetManager, filePath: String): Bitmap {
    var inputStream: InputStream? = null
    try {
        inputStream = assetManager.open(filePath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return bitmap
    }finally {
        inputStream?.close()
    }
}

