/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryItem (
    val image: Uri,
    val text: String
) : Parcelable