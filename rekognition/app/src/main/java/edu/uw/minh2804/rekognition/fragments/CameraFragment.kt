/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import java.io.File
import kotlinx.coroutines.launch

data class CameraOutput(val id: String, val image: File, val requestAnnotator: Annotator, var isProcessed: Boolean = false)

// This class handles the user request of capturing photo.
class CameraFragment : CameraIOProviderFragment() {
    private lateinit var photoStore: PhotoStore
    private val model: CameraViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoStore = PhotoStore(requireActivity())

        val captureButton = view.findViewById<ImageButton>(R.id.button_camera_capture)
        val detectionMode = view.findViewById<TabLayout>(R.id.tab_layout_camera_navigation)

        model.tabPosition.observe(this) { detectionMode.selectTab(detectionMode.getTabAt(it)) }

        detectionMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { model.onTabPositionChanged(tab!!.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        captureButton.setOnClickListener {
            when (detectionMode.selectedTabPosition) {
                0 -> takePhoto(FirebaseFunctionsService.Endpoint.TEXT)
                1 -> takePhoto(FirebaseFunctionsService.Endpoint.OBJECT)
                else -> Log.e(TAG, "Selected tab position not found")
            }
        }
    }

    private fun takePhoto(mode: Annotator) {
        lifecycleScope.launch {
            model.onCameraCapturing()
            val outputFile = photoStore.createOutputFile()
            val id = outputFile.nameWithoutExtension
            if (takePhoto(outputFile.outputStream())) {
                photoStore.save(id, Photo(outputFile))
                model.onCameraCaptured(CameraOutput(id, outputFile, mode))
            } else {
                val e = Exception("Photo capture failed")
                Log.e(TAG, e.toString())
                model.onCameraCaptured(e)
            }
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}