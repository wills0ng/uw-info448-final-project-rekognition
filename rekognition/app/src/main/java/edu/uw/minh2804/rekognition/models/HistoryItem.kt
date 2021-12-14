/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class HistoryItem (
    val id: String,
    val date: Date?,
    val photoUri: Uri,
    val thumbnailUri: Uri?,
    val annotation: AnnotationPair
) : Parcelable

@Parcelize
data class AnnotationPair(
    val type: String,
    val text: String?
) : Parcelable