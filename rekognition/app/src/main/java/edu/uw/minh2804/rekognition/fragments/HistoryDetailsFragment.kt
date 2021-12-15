/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import edu.uw.minh2804.rekognition.AccessibilityActivity
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.databinding.FragmentHistoryDetailsBinding
import edu.uw.minh2804.rekognition.models.HistoryItem
import edu.uw.minh2804.rekognition.viewmodels.HistoryViewModel
import kotlinx.coroutines.launch

class HistoryDetailsFragment : Fragment() {
    // Get reference to shared nav graph scoped ViewModel
    private val viewModel: HistoryViewModel by navGraphViewModels(R.id.nav_graph_history)
    private lateinit var item: HistoryItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.v(TAG, "HistoryDetailsFragment created")
        // Inflate the layout for this fragment with data binding
        val binding = FragmentHistoryDetailsBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Get the item argument passed in through navigation and pass it to the layout
        // to populate the item's details
        item = HistoryDetailsFragmentArgs.fromBundle(requireArguments()).item
        Log.v(TAG,"The info passed in was $item")
        binding.item = item

        // Set click listener for the floating action button to go from history to camera
        binding.fabHistoryDetailsToCamera.setOnClickListener {
            val intent = Intent(activity, AccessibilityActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        Log.d(TAG, "Fragment resuming")
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateHistoryList()
            Log.d(TAG,"Does the item still exist? ${viewModel.doesItemExist(item.id)}")
            if (!viewModel.doesItemExist(item.id)) {
                Log.d(TAG, "Item no longer exists, navigating back to history list")
                findNavController().navigate(
                    HistoryDetailsFragmentDirections.actionBackToHistory()
                )
            }
        }
    }

    companion object {
        private val TAG = HistoryDetailsFragment::class.simpleName
    }
}