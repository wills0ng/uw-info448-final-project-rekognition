package edu.uw.minh2804.rekognition

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {
    private lateinit var annotationStore: AnnotationStore
    private lateinit var photoStore: PhotoStore
    private lateinit var thumbnailStore: ThumbnailStore

    private val model: CameraViewModel by viewModels()

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

        annotationStore = AnnotationStore(this)
        photoStore = PhotoStore(this)
        thumbnailStore = ThumbnailStore(this)

        model.capturedPhoto.observe(this, Observer {
            val id = it.photo.file.nameWithoutExtension

            photoStore.save(id, it.photo)
            thumbnailStore.save(id, it.thumbnail)

            // annotationObserver is only observing once
            val annotationObserver = object : Observer<Annotation?> {
                override fun onChanged(response: Annotation?) {
                    if (response != null) {
                        annotationStore.save(id, response)
                    }
                    model.imageAnnotation.removeObserver(this)
                }
            }
            model.imageAnnotation.observe(this, annotationObserver)
        })
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