package com.lk.tnndemo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_camera_detect.*
import java.util.concurrent.Executors

class CameraDetectFragment: Fragment() {
    companion object{
        const val LENS_FACING = CameraSelector.LENS_FACING_BACK
        private const val TAG = "CameraDetectFragment"
        private const val RC_PERMS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private lateinit var root: View
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null

    private var screenOrientation = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_camera_detect, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initOrientationListener(requireContext())
        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post{startCamera()}
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, RC_PERMS)
        }

    }


    private fun startCamera() {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

//        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
//        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

//        val imageResolutionSize = Size(10000, 5000)
//        Log.d(TAG, "Resolution size: ${imageResolutionSize.width} x ${imageResolutionSize.height}")

        val cameraProvideFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProvideFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProvideFuture.get()
            preview = Preview.Builder()
//                .setTargetAspectRatio(screenAspectRatio)
                .build()
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setTargetResolution(imageResolutionSize)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, DetectFaceAnalyzer())
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(LENS_FACING)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)
                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider())
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

//    private fun setResolution(screenAspectRatio: Int): Size {
//        val width =  when(screenAspectRatio){
//            AspectRatio.RATIO_16_9 -> (MIN_RESOLUTION * RATIO_16_9_VALUE).toInt()
//            else -> (MIN_RESOLUTION * RATIO_4_3_VALUE).toInt()
//        }
//        return Size(width, MIN_RESOLUTION)
//    }

//    private fun aspectRatio(width: Int, height: Int): Int {
//        val previewRatio = max(width, height).toDouble() / min(width, height)
//        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
//            return AspectRatio.RATIO_4_3
//        }
//        return AspectRatio.RATIO_16_9
//    }


    private lateinit var orientationEventListener: OrientationEventListener
    private fun initOrientationListener(context: Context){
        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                screenOrientation = when (orientation) {
                    in 45..134 -> 90
                    in 135..224 ->180
                    in 225..314 -> 270
                    else -> 0
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        orientationEventListener.enable()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener.disable()
    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RC_PERMS && allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        }else {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, RC_PERMS)
        }
    }
}