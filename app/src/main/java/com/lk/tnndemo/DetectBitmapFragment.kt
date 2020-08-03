package com.lk.tnndemo

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_detect_bitmap.*
import java.io.IOException

class DetectBitmapFragment: Fragment() {
    companion object{
        const val TAG = "DetectBitmapFragment"
    }
    private var testBitmap: Bitmap? = null
    private lateinit var ultraFace: UltraFace
    private val NET_H_INPUT = 240
    private val NET_W_INPUT = 320

    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_detect_bitmap, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val modelPath = initModel()
        initViews()

        ultraFace = UltraFace(modelPath, NET_W_INPUT, NET_H_INPUT, 0)
    }

    private fun initViews() {
        try {
            testBitmap = readBitmapFromFile(requireActivity().assets, "test.jpg")
            ivTestImage.setImageBitmap(testBitmap)
        }catch (e: IOException){
            Toast.makeText(requireContext(), "error loading image", Toast.LENGTH_LONG).show()
        }
        btnDetect.setOnClickListener { startDetection() }
    }

    private fun startDetection() {
        testBitmap?.let {
            val scaleBitmap = Bitmap.createScaledBitmap(it, NET_W_INPUT,
                NET_H_INPUT, true)
            val facesInfo = ultraFace.detect(scaleBitmap)
            facesInfo?.let { faces->
                Log.d(MainActivity.TAG, "startDetection: result-size = ${faces.size}")
            }

        }
    }

    @Throws(IOException::class)
    private fun initModel(): String {
        val activity = requireActivity()
        val targetDir: String = activity.filesDir.absolutePath

        //copy detect model to sdcard
        val modelPathsDetector = arrayOf(
            "version-slim-320_simplified.tnnmodel",
            "version-slim-320_simplified.tnnproto"
        )
        for (modelPath in modelPathsDetector) {
            val interModelFilePath = "$targetDir/$modelPath"
            copyAsset(activity.assets, modelPath, interModelFilePath)
        }
        return targetDir
    }
}