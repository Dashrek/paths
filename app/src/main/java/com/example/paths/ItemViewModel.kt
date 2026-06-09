package com.example.paths

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ItemViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    fun rateRoute(routeId: String, userId: String, rating: Int) {
        viewModelScope.launch {
            val data = hashMapOf(
                "routeId" to routeId,
                "userId" to userId,
                "rating" to rating,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("routeRatings").add(data)
        }
    }

    private var listenerRegistration: ListenerRegistration? = null
    
    // Stan ładowania
    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    // Flaga, która pozwoli nam filtrować dane po przypisaniu do konkretnego VM
    private var typeFilter: Boolean? = null
    private var currentUserId: String? = null

    fun setFilter(isRower: Boolean, userId: String? = null) {
        typeFilter = isRower
        currentUserId = userId
        fetchItems()
    }

    fun uploadRoute(
        item: Item,
        imageUris: List<Uri>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val uploadedUrls = mutableListOf<String>()
                
                // 1. Upload zdjęć do Storage
                imageUris.forEachIndexed { index, uri ->
                    val fileName = "route_${System.currentTimeMillis()}_$index.jpg"
                    val ref = storage.reference.child("routes/${item.ownerId}/$fileName")
                    ref.putFile(uri).await()
                    val downloadUrl = ref.downloadUrl.await().toString()
                    uploadedUrls.add(downloadUrl)
                }

                // 2. Przygotowanie finalnego obiektu Item
                val avgPoint = if (item.pathPoints.isNotEmpty()) AveragePoint(item.pathPoints) else null
                val finalItem = item.copy(imageUrls = uploadedUrls, startLocation = avgPoint)
                
                // 3. Zapis do Firestore
                db.collection("remoteRoutes").add(finalItem).await()
                
                _isUploading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isUploading.value = false
                onFailure(e)
            }
        }
    }

    fun fetchItems() {
        // Usuwamy stary listener przed dodaniem nowego, aby uniknąć wycieków i duplikacji
        listenerRegistration?.remove()

        val query = db.collection("remoteRoutes")
        
        // Jeśli ustawiliśmy filtr, pobieramy tylko konkretny typ
        val finalQuery = if (typeFilter != null) {
            query.whereEqualTo("type", typeFilter)
        } else {
            query
        }

        listenerRegistration = finalQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("FirestoreError", "Błąd pobierania: ${error.message}")
                return@addSnapshotListener
            }

            val itemsFromDb = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Item::class.java)
            }?.filter { item ->
                !item.privateStatus || item.ownerId == currentUserId
            } ?: emptyList()

            _items.value = itemsFromDb
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
