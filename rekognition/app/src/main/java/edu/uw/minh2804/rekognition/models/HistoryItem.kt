/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.models

import android.net.Uri
import android.os.Parcelable
import edu.uw.minh2804.rekognition.stores.Annotation
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryItem (
    val id: String,
    val photoUri: Uri,
    val thumbnailUri: Uri?,
    val annotations: String?
) : Parcelable