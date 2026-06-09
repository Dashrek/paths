package com.example.paths
import com.google.firebase.firestore.GeoPoint
fun AveragePoint(points: List<GeoPoint>): GeoPoint {
    val licznik = points.size;
    val lat = points.map { it.latitude }.sum() / licznik
    val lng = points.map { it.longitude }.sum() / licznik
    return GeoPoint(lat, lng)
}
