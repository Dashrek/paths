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
import kotlinx.coroutines.Dispatchers

class ItemViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    fun rateRoute(routeId: String, userId: String, rating: Int) {
        viewModelScope.launch {
            val ratingId = "${userId}_$routeId"
            if (rating == 0) {
                // Usuwanie oceny
                try {
                    db.collection("routeRatings").document(ratingId).delete().await()
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreError", "Błąd usuwania oceny: ${e.message}")
                }
            } else {
                // Dodawanie/Aktualizacja oceny
                val data = hashMapOf(
                    "routeId" to routeId,
                    "userId" to userId,
                    "rating" to rating,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                db.collection("routeRatings").document(ratingId).set(data)
            }
        }
    }

    fun updateRouteField(routeId: String, field: String, value: Any) {
        viewModelScope.launch {
            try {
                db.collection("remoteRoutes").document(routeId)
                    .update(field, value, "updatedAt", com.google.firebase.Timestamp.now())
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("FirestoreError", "Błąd aktualizacji pola $field: ${e.message}")
            }
        }
    }

    fun updateRouteFields(routeId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val finalUpdates = updates.toMutableMap()
                finalUpdates["updatedAt"] = com.google.firebase.Timestamp.now()
                db.collection("remoteRoutes").document(routeId)
                    .update(finalUpdates)
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("FirestoreError", "Błąd aktualizacji pól: ${e.message}")
            }
        }
    }

    fun deleteImageFromRoute(routeId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                // 1. Usuń ze Storage
                storage.getReferenceFromUrl(imageUrl).delete().await()
                
                // 2. Pobierz aktualny dokument
                val doc = db.collection("remoteRoutes").document(routeId).get().await()
                val currentImages = doc.get("imageUrls") as? List<String> ?: emptyList()
                
                // 3. Zaktualizuj listę
                val newImages = currentImages.filter { it != imageUrl }
                db.collection("remoteRoutes").document(routeId)
                    .update("imageUrls", newImages, "updatedAt", com.google.firebase.Timestamp.now())
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("FirestoreError", "Błąd usuwania zdjęcia: ${e.message}")
            }
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

            viewModelScope.launch(Dispatchers.Default) {
                val itemsFromDb = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.copy(id = doc.id)
                }?.filter { item ->
                    !item.privateStatus || item.ownerId == currentUserId
                } ?: emptyList()

                _items.value = itemsFromDb
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
