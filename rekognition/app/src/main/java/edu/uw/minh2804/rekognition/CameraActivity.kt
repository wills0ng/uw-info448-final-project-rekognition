package edu.uw.minh2804.rekognition

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.launch

class CameraActivity : ActionBarActivity(R.layout.activity_camera) {
    private val model: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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