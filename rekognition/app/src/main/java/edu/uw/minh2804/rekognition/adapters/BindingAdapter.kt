/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.adapters

import android.graphics.Bitmap
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uw.minh2804.rekognition.models.HistoryItem
import java.util.*

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

@BindingAdapter("relativeTime")
fun displayRelativeTime(textView: TextView, date: Date) {
    textView.text = DateUtils.getRelativeTimeSpanString(
        date.getTime(),
        Calendar.getInstance().getTimeInMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )
}