package com.example.paths

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user = _user.asStateFlow()

    // ID użytkownika, które będziemy wstawiać do ownerId przy dodawaniu tras
    val currentUserId: String get() = _user.value?.uid ?: ""

    // Symulacja zgód
    private val _sharePhotos = MutableStateFlow(false)
    val sharePhotos = _sharePhotos.asStateFlow()

    private val _shareLocation = MutableStateFlow(false)
    val shareLocation = _shareLocation.asStateFlow()

    // Nowe stany filtrów
    private val _radiusKm = MutableStateFlow(25f) // domyślnie 25km
    val radiusKm = _radiusKm.asStateFlow()

    private val _minRating = MutableStateFlow(0f)
    val minRating = _minRating.asStateFlow()

    private val _showOnlyMine = MutableStateFlow(false)
    val showOnlyMine = _showOnlyMine.asStateFlow()

    private val _showOnlyLiked = MutableStateFlow(false)
    val showOnlyLiked = _showOnlyLiked.asStateFlow()

    // Motyw i Style
    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = systemowy
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _colorSchemeIndex = MutableStateFlow(0) // Indeks wybranego schematu (0-4)
    val colorSchemeIndex = _colorSchemeIndex.asStateFlow()

    private val _showMinimaps = MutableStateFlow(false)
    val showMinimaps = _showMinimaps.asStateFlow()

    fun setShowMinimaps(value: Boolean) {
        _showMinimaps.value = value
    }

    fun setDarkMode(value: Boolean?) {
        _isDarkMode.value = value
    }

    fun setColorSchemeIndex(index: Int) {
        _colorSchemeIndex.value = index
    }

    fun signInWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    // Do celów testowych (projekt studencki) - szybkie logowanie bez Google
    fun mockLogin() {
        // To tylko symulacja w UI, w realu potrzebny idToken
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }

    fun setSharePhotos(value: Boolean) {
        _sharePhotos.value = value
    }

    fun setShareLocation(value: Boolean) {
        _shareLocation.value = value
    }

    fun setRadius(value: Float) {
        _radiusKm.value = value
    }

    fun setMinRating(value: Float) {
        _minRating.value = value
    }

    fun toggleOnlyMine() {
        _showOnlyMine.value = !_showOnlyMine.value
    }

    fun toggleOnlyLiked() {
        _showOnlyLiked.value = !_showOnlyLiked.value
    }
}
