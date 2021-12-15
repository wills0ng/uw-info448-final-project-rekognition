/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
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

// This fragment is responsible for handling user inputs and updating it to the view model.
// User input includes selecting one of the detection options and taking picture.
class AccessibilityFragment : Fragment(R.layout.fragment_accessibility) {
    private lateinit var photoStore: PhotoStore
    private val model: CameraViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoStore = PhotoStore(requireActivity())

        val captureButton = view.findViewById<ImageButton>(R.id.button_camera_capture)
        val optionsTab = view.findViewById<TabLayout>(R.id.tab_layout_camera_navigation)

        captureButton.setOnClickListener {
            when (optionsTab.selectedTabPosition) {
                0 -> takePhoto(FirebaseFunctionsService.Annotator.TEXT)
                1 -> takePhoto(FirebaseFunctionsService.Annotator.OBJECT)
                else -> Log.e(TAG, "Selected tab not found")
            }
        }

        optionsTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { model.onTabPositionChanged(tab!!.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        model.tabPosition.observe(this) { optionsTab.selectTab(optionsTab.getTabAt(it)) }
    }

    private fun takePhoto(mode: Annotator) {
        lifecycleScope.launch {
            val camera = childFragmentManager.fragments[0] as CameraFragment
            val outputFile = photoStore.createOutputFile()
            val id = outputFile.nameWithoutExtension
            try {
                camera.takePhoto(outputFile.outputStream())
                photoStore.save(id, Photo(outputFile))
                model.onCameraCaptured(CameraOutput(id, outputFile, mode))
            } catch (e: CameraNotDetectedException) {
                model.onCameraCaptureFailed(e)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                model.onCameraCaptureFailed(getString(R.string.internal_error_message))
            }
        }
    }

    companion object {
        private const val TAG = "RecognitionFragment"
    }
}