/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

// Represents a history item to be displayed in the history RecyclerView and details fragment
@Parcelize
data class HistoryItem (
    val id: String,
    val date: Date?,
    val photoUri: Uri,
    val thumbnailUri: Uri?,
    val annotation: AnnotationPair
) : Parcelable

// A pair of attributes for each image annotation.
// The annotation type (Text, Object, No Result) and text string
@Parcelize
data class AnnotationPair(
    val type: String,
    val text: String?
) : Parcelable