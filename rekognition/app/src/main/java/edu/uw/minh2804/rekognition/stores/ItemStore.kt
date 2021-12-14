package edu.uw.minh2804.rekognition.stores

data class SavedItem<T>(
    val id: String,
    val item: T
)

interface ItemStore<T> {
    val items: List<Lazy<SavedItem<T>>>
    suspend fun findItem(id: String): SavedItem<T>?
    suspend fun save(id: String, item: T): SavedItem<T>
}