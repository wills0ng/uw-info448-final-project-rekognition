package edu.uw.minh2804.rekognition.stores

data class SavedItem<T>(
    val id: String,
    val item: T
)

interface ItemStore<T> {
    val items: List<SavedItem<T>>
    fun findItem(id: String): SavedItem<T>?
    fun save(id: String, item: T): SavedItem<T>
}