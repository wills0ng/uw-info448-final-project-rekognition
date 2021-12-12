/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uw.minh2804.rekognition.databinding.HistoryItemBinding
import edu.uw.minh2804.rekognition.models.HistoryItem

class HistoryItemAdapter : ListAdapter<HistoryItem, HistoryItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        // Add a click listener to each item to go to the details view
        // TODO: uncomment after adding navigation
        /*holder.itemView.setOnClickListener {
            val action = NavigationDirections.seeDetails(item)
            it.findNavController().navigate(action)
        }*/
    }

    class ViewHolder(private val binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.image == newItem.image
        }
    }
}