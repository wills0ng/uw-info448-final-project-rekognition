/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.stores

import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.services.AnnotateImageResponse
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

@Parcelize
data class Annotation(val result: AnnotateImageResponse) : Parcelable

// This store is responsible for reading and writing annotation data to the device's internal storage.
class AnnotationStore(private val context: FragmentActivity) : ItemStore<Annotation> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".annotations").also { it.mkdirs() }
        }
    }

    override val items: List<Lazy<SavedItem<Annotation>>>
        get() = directory.listFiles()!!.map {
            lazy { readAnnotationFrom(it) }
        }

    override suspend fun findItem(id: String): SavedItem<Annotation>? {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            val file = directory.listFiles()!!.firstOrNull {
                it.nameWithoutExtension == id
            }
            if (file != null) readAnnotationFrom(file) else null
        }
    }

    // Save the item into internal storage, with the id as file name.
    // Returns a SavedItem wrapper as indicator of success, else throw exceptions.
    override suspend fun save(id: String, item: Annotation): SavedItem<Annotation> {
        return withContext(NonCancellable + Dispatchers.IO) {
            File(directory, "$id.json").also {
                it.writeText(Gson().toJson(item))
            }
            SavedItem(id, item)
        }
    }

    private fun readAnnotationFrom(file: File): SavedItem<Annotation> {
        val item = file.reader().let {
            val result = Gson().fromJson(it, Annotation::class.java)
            it.close()
            result
        }
        return SavedItem(file.nameWithoutExtension, item)
    }
}