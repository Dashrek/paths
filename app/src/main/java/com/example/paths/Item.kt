package com.example.paths

import com.google.firebase.firestore.DocumentId

// Pomocnicza klasa dla koordynatów (łatwa do zapisu w Firebase)
data class RoutePoint(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

class Item(
    @DocumentId
    var id: String = "",           // ID z bazy danych (String dla Firebase)
    var name: String = "",
    var type: Boolean = false,      // true dla roweru, false dla pieszego
    var ownerId: String = "",
    var scores: List<Int> = emptyList(),
    var imageUrls: List<String> = emptyList(), // Max 10 linków
    var shortDescription: String = "",
    var longDescription: String = "",
    var userRating: Float? = null,
    var averageRating: Float = 0.0f,
    var mapShortcutUrl: String = "" // URL do statycznego obrazka mapy
) {
    fun getImages(): List<String> {
        return imageUrls
    }
}
