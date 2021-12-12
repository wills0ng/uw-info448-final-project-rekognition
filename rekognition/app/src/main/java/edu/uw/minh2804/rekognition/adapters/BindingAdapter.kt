/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.adapters

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uw.minh2804.rekognition.models.HistoryItem

// BindingAdapter for RecyclerView
@BindingAdapter("historyList")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<HistoryItem>?) {
    if (data != null) {
        val adapter = recyclerView.adapter as HistoryItemAdapter
        adapter.submitList(data)
    }
}

@BindingAdapter("imageThumbnail")
fun loadImageBitmap(imageView: ImageView, thumbnail: Bitmap) {
    imageView.setImageBitmap(thumbnail)
}