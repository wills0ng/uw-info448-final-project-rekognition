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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import edu.uw.minh2804.rekognition.CameraActivity
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.adapters.HistoryItemAdapter
import edu.uw.minh2804.rekognition.databinding.FragmentHistoryBinding
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModel
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    // Initialize a nav graph scoped ViewModel
    private val viewModel: HistoryViewModel by navGraphViewModels(R.id.nav_graph_history)
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryFragment created")

        // Inflate the layout for this fragment with data binding
        binding = FragmentHistoryBinding.inflate(inflater)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Getting results from ImageProcessingStore")

        // IMPORTANT: Stores can only be initialized after onViewCreated lifecycle
        viewModel.setDataStores(
            PhotoStore(requireActivity()),
            ThumbnailStore(requireActivity()),
            AnnotationStore(requireActivity())
        )
        viewLifecycleOwner.lifecycleScope.launch {
            refreshHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Fragment resumed")
        viewLifecycleOwner.lifecycleScope.launch {
            refreshHistory()
        }
    }

    private suspend fun refreshHistory() {
        viewModel.updateHistoryList()
        viewModel.historyList.value?.let {
            Log.v(TAG, "The size of the current history list is: ${it.size}")
            if(it.isEmpty()) {
                binding.textViewHistoryIsEmpty?.visibility = View.VISIBLE
            } else {
                binding.textViewHistoryIsEmpty?.visibility = View.GONE
            }
        }
    }

    companion object {
        private val TAG = HistoryFragment::class.simpleName
    }
}