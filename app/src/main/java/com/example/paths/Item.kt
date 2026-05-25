package com.example.paths

// Pomocnicza klasa dla koordynatów (łatwa do zapisu w Firebase)
data class RoutePoint(
    var lat: Double = 0.0,
    var lng: Double = 0.0
)

class Item(
    var id: String = "",           // ID z bazy danych (String dla Firebase)
    var name: String = "",
    var scores: List<Int> = emptyList(),
    public final var imageUrls: List<String> = emptyList(), // Max 10 linków
    var coordinates: List<RoutePoint> = emptyList(), // Punkty na mapę
    var shortDescription: String = "",
    var longDescription: String = "",
    // Para: (Twoja ocena Float? - może być null, Średnia ocen Float)
    var rating: Pair<Float?, Float> = Pair(null, 0.0f),
    var mapShortcutUrl: String = "" // URL do statycznego obrazka mapy
) {
    // Tutaj możesz dopisać swoje własne funkcje

        fun getImages(): List<String> {
            return imageUrls
        }

}
