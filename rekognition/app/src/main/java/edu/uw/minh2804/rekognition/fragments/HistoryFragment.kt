/**
 * Will Song: Wrote most of this file.
 * Tom Nguyen: Wrote code for the onViewCreated() method and the `histories` attribute.
 * */

package edu.uw.minh2804.rekognition.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import edu.uw.minh2804.rekognition.CameraActivity
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.adapters.HistoryItemAdapter
import edu.uw.minh2804.rekognition.databinding.FragmentHistoryBinding
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModel

class HistoryFragment : Fragment() {
    // Initialize a nav graph scoped ViewModel
    private val viewModel: HistoryViewModel by navGraphViewModels(R.id.nav_graph_history)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryFragment created")

        // Inflate the layout for this fragment with data binding
        val binding = FragmentHistoryBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the HistoryViewModel
        binding.viewModel = viewModel

        // Set the adapter for the RecyclerView
        binding.recyclerViewHistory.adapter = HistoryItemAdapter()

        // Set click listener for the floating action button to go from history to camera
        binding.fabHistoryToCamera.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        // Add up navigation to toolbar
        binding.toolbarHistory.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarHistory.setNavigationOnClickListener {
            requireActivity().onNavigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Getting results from ImageProcessingStore")

        // IMPORTANT: Stores can only be initialized after onViewCreated lifecycle
        AnnotationStore(requireActivity())
        PhotoStore(requireActivity())
        ThumbnailStore(requireActivity())
    }

/*

    */
/**
     * Copy exif data from original image to preserve orientation info
     * References:
     * 1. https://stackoverflow.com/questions/13596500/android-image-resizing-and-preserving-exif-data-orientation-rotation-etc
     * 2. https://stackoverflow.com/questions/49407931/filenotfoundexception-when-using-exifinterface
     * 3. https://stackoverflow.com/questions/66107689/androidx-exifinterface-crashes-when-try-to-saveattributes-write-failed-ebadf
     *//*

    private fun copyExifDataFromOriginal(originalImage: File, newImage: File) {
        val originalImageExif = ExifInterface(originalImage.absolutePath)
        originalImageExif.getAttribute(ExifInterface.TAG_ORIENTATION)?.let { orientationExif ->
            val newImageFileDescriptor = context.contentResolver.openFileDescriptor(
                Uri.fromFile(newImage), "rw")!!.fileDescriptor
            val newImageExif = ExifInterface(newImageFileDescriptor)
            newImageExif.setAttribute(ExifInterface.TAG_ORIENTATION, orientationExif)
            newImageExif.saveAttributes()
        }
    }
*/

    companion object {
        private val TAG = HistoryFragment::class.simpleName
    }
}