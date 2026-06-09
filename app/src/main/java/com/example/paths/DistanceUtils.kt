package com.example.paths

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

fun calculatePathDistance(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var totalDistance = 0f
    for (i in 0 until points.size - 1) {
        val results = FloatArray(1)
        Location.distanceBetween(
            points[i].latitude, points[i].longitude,
            points[i + 1].latitude, points[i + 1].longitude,
            results
        )
        totalDistance += results[0]
    }
    return (totalDistance / 1000.0) // Wynik w kilometrach
}

fun calculatePathDistanceGeo(points: List<GeoPoint>): Double {
    if (points.size < 2) return 0.0
    var totalDistance = 0f
    for (i in 0 until points.size - 1) {
        val results = FloatArray(1)
        Location.distanceBetween(
            points[i].latitude, points[i].longitude,
            points[i + 1].latitude, points[i + 1].longitude,
            results
        )
        totalDistance += results[0]
    }
    return (totalDistance / 1000.0)
}
