/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.uw.minh2804.rekognition.CameraActivity
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.databinding.FragmentHistoryDetailsBinding

class HistoryDetailsFragment : Fragment() {
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
        val item = HistoryDetailsFragmentArgs.fromBundle(requireArguments()).item
        Log.v(TAG,"The info passed in was $item")
        binding.item = item

        // Add up navigation to toolbar
        binding.toolbarHistoryDetails.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarHistoryDetails.setNavigationOnClickListener {
            requireActivity().onNavigateUp()
        }

        // Set click listener for the floating action button to go from history to camera
        binding.fabHistoryDetailsToCamera.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    companion object {
        private val TAG = HistoryDetailsFragment::class.simpleName
    }
}