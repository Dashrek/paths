package com.example.paths

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ItemViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()

    // Flaga, która pozwoli nam filtrować dane po przypisaniu do konkretnego VM
    private var typeFilter: Boolean? = null

    fun setFilter(isRower: Boolean) {
        typeFilter = isRower
        fetchItems()
    }

    fun fetchItems() {
        val query = db.collection("remoteRoutes")
        
        // Jeśli ustawiliśmy filtr, pobieramy tylko konkretny typ
        val finalQuery = if (typeFilter != null) {
            query.whereEqualTo("type", typeFilter)
        } else {
            query
        }

        finalQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("FirestoreError", "Błąd pobierania: ${error.message}")
                return@addSnapshotListener
            }

            val itemsFromDb = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Item::class.java)
            } ?: emptyList()

            _items.value = itemsFromDb
        }
    }
}
