/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private companion object {
        const val TAG = "CameraActivity"
    }
}