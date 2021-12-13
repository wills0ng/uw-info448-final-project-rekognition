package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<TabLayout>(R.id.tab_layout_camera_navigation).addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Log.v(TAG, tab!!.text.toString())
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    Log.v(TAG, tab!!.text.toString())
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    Log.v(TAG, tab!!.text.toString())
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}