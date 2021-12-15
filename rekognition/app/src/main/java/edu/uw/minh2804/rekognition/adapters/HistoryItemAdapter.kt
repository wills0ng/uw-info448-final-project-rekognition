/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uw.minh2804.rekognition.databinding.HistoryItemBinding
import edu.uw.minh2804.rekognition.fragments.HistoryFragmentDirections
import edu.uw.minh2804.rekognition.models.HistoryItem


// ListAdapter for the History RecyclerView
class HistoryItemAdapter : ListAdapter<HistoryItem, HistoryItemAdapter.ViewHolder>(DiffCallback()) {
    class ViewHolder(private val binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    // Use data binding to inflate view and provide binding to ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // When binding ViewHolder, add a click listener to each item to go to the details view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            val action = HistoryFragmentDirections.actionSeeHistoryDetails(item)
            it.findNavController().navigate(action)
        }
    }
}

// Callbacks for calculating diffs between two non-null items in a list
class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    // Do two objects represent the same items?
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }

    // Do two objects have the same data?
    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.id == newItem.id
    }
}