package com.example.paths

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

class AddRouteViewModel : ViewModel() {
    var name by mutableStateOf("")
    var isRower by mutableStateOf(true)
    var shortDesc by mutableStateOf("")
    var longDesc by mutableStateOf("")
    val selectedImages = mutableStateListOf<Uri>()
    val pathPoints = mutableStateListOf<LatLng>()
    var isMapFullScreen by mutableStateOf(false)
    var privateStatus by mutableStateOf(false)

    fun clearForm() {
        name = ""
        isRower = true
        shortDesc = ""
        longDesc = ""
        selectedImages.clear()
        pathPoints.clear()
        isMapFullScreen = false
        privateStatus = false
    }

    fun getNewItem(ownerId: String?): Item {
        val distance = calculatePathDistance(pathPoints.toList())
        return Item(
            name = name,
            type = isRower,
            shortDescription = shortDesc,
            longDescription = longDesc,
            ownerId = ownerId ?: "",
            startLocation = pathPoints.firstOrNull()?.let { GeoPoint(it.latitude, it.longitude) } ?: GeoPoint(0.0, 0.0),
            pathPoints = pathPoints.map { GeoPoint(it.latitude, it.longitude) },
            distance = distance,
            privateStatus = privateStatus
        )
    }
}
