/**
 * Will Song: Wrote code for copyExifDataFromOriginal() and using it to copy EXIF to thumbnail img.
 */
package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.scaleDown
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageProcessingResultStatusCode {
    const val RESULT_FOUND = 1
    const val RESULT_NOT_FOUND = 2
}

data class ImageProcessingResult (
    val photoUriPath: String,
    val thumbnailUriPath: String,
    val result: String,
    val statusCode: Int
) : Serializable

class ImageProcessingStore(private val context: FragmentActivity) {
    private val imageDirectory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    private val resultDirectory: File by lazy {
        context.filesDir.let {
            File(it, context.resources.getString(R.string.app_name) + ".results").apply { mkdirs() }
        }
    }

    private val thumbnailDirectory: File by lazy {
        context.filesDir.let {
            File(it, context.resources.getString(R.string.app_name) + ".thumbnails").apply { mkdirs() }
        }
    }

    val results: Array<ImageProcessingResult>
        get() = resultDirectory.listFiles()!!.map { file ->
                file.inputStream().let { fileInputStream ->
                    ObjectInputStream(fileInputStream).let {
                        it.readObject() as ImageProcessingResult
                    }
                }
            } .toTypedArray()

    // https://mkyong.com/java/how-to-read-and-write-java-object-to-a-file/
    // Save results into file
    fun saveResultToFile(savedImageFile: File, result: ImageProcessingResult): File {
        return createProcessedResultOutputFileFrom(savedImageFile).also { file ->
            file.outputStream().let { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).let {
                    it.writeObject(result)
                    it.close()
                }
                fileOutputStream.close()
            }
        }
    }

    fun saveImageToThumbnailFile(savedImageFile: File): File {
        val savedImageUriPath = Uri.fromFile(savedImageFile).path!!
        val scaledDownBitmap = BitmapFactory.decodeFile(savedImageUriPath).scaleDown(640)
        return createThumbnailOutputFileFrom(savedImageFile).also {
            scaledDownBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it.outputStream())
        }.also {
            copyExifDataFromOriginal(savedImageFile, it)
        }
    }

    fun createUniqueImageOutputFile(): File {
        val uniqueFileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        return File(imageDirectory, uniqueFileName)
    }

    private fun createThumbnailOutputFileFrom(originalImage: File): File {
        val fileName = originalImage.nameWithoutExtension + ".jpg"
        return File(thumbnailDirectory, fileName)
    }

    /**
     * Copy exif data from original image to preserve orientation info
     * References:
     * 1. https://stackoverflow.com/questions/13596500/android-image-resizing-and-preserving-exif-data-orientation-rotation-etc
     * 2. https://stackoverflow.com/questions/49407931/filenotfoundexception-when-using-exifinterface
     * 3. https://stackoverflow.com/questions/66107689/androidx-exifinterface-crashes-when-try-to-saveattributes-write-failed-ebadf
     */
    private fun copyExifDataFromOriginal(originalImage: File, newImage: File) {
        val originalImageExif = ExifInterface(originalImage.absolutePath)
        originalImageExif.getAttribute(ExifInterface.TAG_ORIENTATION)?.let { orientationExif ->
            val newImageFileDescriptor = context.contentResolver.openFileDescriptor(
                Uri.fromFile(newImage), "rw")!!.fileDescriptor
            val newImageExif = ExifInterface(newImageFileDescriptor)
            newImageExif.setAttribute(ExifInterface.TAG_ORIENTATION, orientationExif)
            newImageExif.saveAttributes()
        }
    }

    private fun createProcessedResultOutputFileFrom(originalImage: File): File {
        val fileName = originalImage.nameWithoutExtension + ".ser"
        return File(resultDirectory, fileName)
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}