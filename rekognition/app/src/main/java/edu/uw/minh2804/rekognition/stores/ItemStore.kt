/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.stores

// This class is used to provide semantic to any type of object.
// In other word, any object that it is holding is a securely stored somewhere in the store,
// and can be retrieved again by the id.
data class SavedItem<T>(
    val id: String,
    val item: T
)

// This interface is used to provide the base requirements of being a store for the app.
interface ItemStore<T> {
    val items: List<Lazy<SavedItem<T>>>
    suspend fun findItem(id: String): SavedItem<T>?

    // Save the item into store with the id attached.
    // Returns a SavedItem wrapper as indicator of success, else throw exceptions.
    suspend fun save(id: String, item: T): SavedItem<T>
}