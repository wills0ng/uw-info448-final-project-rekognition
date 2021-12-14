package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.launch

class CameraActivity : ActionBarActivity(R.layout.activity_camera) {
    private val model: CameraViewModel by viewModels()

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

        val annotationStore = AnnotationStore(this)
        val photoStore = PhotoStore(this)
        val thumbnailStore = ThumbnailStore(this)

        @Suppress("DeferredResultUnused")
        model.capturedPhoto.observe(this, Observer {
            val id = it.photo.file.nameWithoutExtension

            lifecycleScope.launch {
                photoStore.save(id, it.photo)
                thumbnailStore.save(id, it.thumbnail)
            }

            // This observer will only observe and only invoke once
            val imageAnnotationObserver = object : Observer<Annotation?> {
                override fun onChanged(response: Annotation?) {
                    if (response != null) {
                        lifecycleScope.launch {
                            annotationStore.save(id, response)
                        }
                    }
                    model.imageAnnotation.removeObserver(this)
                }
            }
            model.imageAnnotation.observe(this, imageAnnotationObserver)
        })
    }

    companion object {
         private const val TAG = "CameraActivity"
    }
}