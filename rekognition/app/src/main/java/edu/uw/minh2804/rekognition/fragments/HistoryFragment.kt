/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.adapters.HistoryItemAdapter
import edu.uw.minh2804.rekognition.databinding.FragmentHistoryBinding
import edu.uw.minh2804.rekognition.stores.ImageProcessingResult
import edu.uw.minh2804.rekognition.stores.ImageProcessingStore
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModel
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModelFactory
import java.io.File

class HistoryFragment : Fragment() {
    // Initialize a nav graph scoped ViewModel
    private val viewModel: HistoryViewModel by navGraphViewModels(R.id.nav_graph_history) {
        HistoryViewModelFactory(getImagesDirectory())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "HistoryFragment created")

        // Inflate the layout for this fragment with data binding
        val binding = FragmentHistoryBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the HistoryViewModel
        binding.viewModel = viewModel

        // Set the adapter for the RecyclerView
        binding.recyclerView.adapter = HistoryItemAdapter()

        return binding.root
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private lateinit var histories: Array<ImageProcessingResult>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // IMPORTANT: ImageProcessingStore can only be initialized after onViewCreated lifecycle
        histories = ImageProcessingStore(requireActivity()).results

        for (history in histories) {
            Log.v(TAG, history.photoUriPath)
            Log.v(TAG, history.thumbnailUriPath)
            Log.v(TAG, history.result)
            Log.v(TAG, history.statusCode.toString())
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Function to get the directory the images / text are stored in.
     */
    private fun getImagesDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
    }

    companion object {
        private val TAG = HistoryFragment::class.simpleName
    }
}