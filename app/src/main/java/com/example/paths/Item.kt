package com.example.paths

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Pomocnicza klasa dla koordynatów (nadal przydatna w innych miejscach)
data class RoutePoint(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

data class Item(
    @DocumentId
    var id: String = "",           // ID z bazy danych
    var name: String = "",
    var type: Boolean = false,      // true dla roweru, false dla pieszego
    var ownerId: String = "",
    var scores: List<Int> = emptyList(),
    var imageUrls: List<String> = emptyList(), 
    var shortDescription: String = "",
    var longDescription: String = "",
    var userRating: Float? = null,
    var averageRating: Double = 0.0,
    var totalRatings: Int = 0,
    var distance: Double = 0.0,
    var pathPoints: List<GeoPoint> = emptyList(),
    var startLocation: GeoPoint? = null,
    var mapShortcutUrl: String = "",
    var privateStatus: Boolean = false,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) {
    fun getImages(): List<String> {
        return imageUrls
    }
}
