package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.launch

class CameraActivity : ActionBarActivity(R.layout.activity_camera) {
    private val model: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TabLayout>(R.id.tab_layout_camera_navigation).addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Log.v(TAG, "${tab!!.text.toString()} tab selected")
                    model.onSetCameraTab(tabPosition = tab.position)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    Log.v(TAG, "${tab!!.text.toString()} tab reselected")
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    Log.v(TAG, "${tab!!.text.toString()} tab unselected")
                }
            }
        )

        val photoStore = PhotoStore(this)
        val thumbnailStore = ThumbnailStore(this)

        model.capturedPhoto.observe(this, Observer {
            val id = it.photo.file.nameWithoutExtension

            lifecycleScope.launch {
                photoStore.save(id, it.photo)
            }

            lifecycleScope.launch {
                thumbnailStore.save(id, it.thumbnail)
            }
        })
    }

    companion object {
         private const val TAG = "CameraActivity"
    }
}