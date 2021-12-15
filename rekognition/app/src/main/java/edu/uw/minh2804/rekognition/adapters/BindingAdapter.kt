/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.adapters

import android.text.format.DateUtils
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

// BindingAdapter for displaying relative time spans (e.g. 3 hours ago)
@BindingAdapter("relativeTimeSpan")
fun displayRelativeTimeSpan(textView: TextView, date: Date) {
    textView.text = DateUtils.getRelativeTimeSpanString(date.time)
}

//BindingAdapter for displaying relative date time
@BindingAdapter("relativeDateTime")
fun displayRelativeDateTime(textView: TextView, date: Date) {
    textView.text = DateUtils.getRelativeDateTimeString(
        textView.context,
        date.time,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.DAY_IN_MILLIS,
        DateUtils.FORMAT_SHOW_TIME
    )
}