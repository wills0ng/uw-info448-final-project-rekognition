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
import edu.uw.minh2804.rekognition.stores.ImageProcessingResult
import edu.uw.minh2804.rekognition.stores.ImageProcessingStore
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModel

class HistoryFragment : Fragment() {
    // Initialize a nav graph scoped ViewModel
    private val viewModel: HistoryViewModel by navGraphViewModels(R.id.nav_graph_history)
    private lateinit var histories: Array<ImageProcessingResult>

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
        binding.recyclerView.adapter = HistoryItemAdapter()

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
        // IMPORTANT: ImageProcessingStore can only be initialized after onViewCreated lifecycle
        histories = ImageProcessingStore(requireActivity()).results

        for (history in histories) {
            Log.v(TAG, history.photoUriPath)
            Log.v(TAG, history.thumbnailUriPath)
            Log.v(TAG, history.result)
            Log.v(TAG, history.statusCode.toString())
        }
        // Populate the ViewModel with history data
        viewModel.populateHistoryList(histories)
    }

    companion object {
        private val TAG = HistoryFragment::class.simpleName
    }
}