package com.lk.tnndemo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.loadLibrary("demo")
        setUpNavigation()
    }

    private fun setUpNavigation() {
        // Finding the Navigation Controller
        val navController = findNavController(R.id.nav_host_fragment_container)

        // Setting Navigation Controller with the BottomNavigationView
        bottomNav.setupWithNavController(navController)
    }


}