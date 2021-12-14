/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryItem (
    val photoUri: Uri?,
    val thumbnailUri: Uri?,
    val annotations: String
) : Parcelable