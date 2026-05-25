package com.example.paths

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ItemViewModel : ViewModel() {
    // To jest Twoja "klasa lista itemów" - stan dla Compose
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items = _items.asStateFlow()
    init{
        loadMockData()
    }
    fun fetchItems() {
        // Tu docelowo logika pobierania z Firebase
    }
    private fun loadMockData() {
        _items.value = listOf(
            Item(
                id = "1",
                name = "Trasa Morskie Oko",
                imageUrls = listOf(
                    "https://picsum.photos/id/10/200/300",
                    "https://picsum.photos/id/11/200/300",
                    "https://picsum.photos/id/12/200/300"
                ),
                shortDescription = "Piękne widoki, łatwa trasa.",
                longDescription = "take(3): Zgodnie z wymaganiem bierze maksymalnie 3 pierwsze linki z listy (nawet jeśli w bazie jest 10).\n" +
                        "2.\n" +
                        "graphicsLayer: To tutaj dzieje się magia – obracamy zdjęcie wokół osi Z (rotationZ) i przesuwamy je w lewo/prawo (translationX)."
            ),
            Item(
                id = "2",
                name = "Pętla Beskidzka",
                imageUrls = listOf(
                    "https://picsum.photos/id/20/200/300",
                    "https://picsum.photos/id/21/200/300"
                ),
                shortDescription = "Wymagające podjazdy.",
                longDescription = "Box: Nakłada zdjęcia na siebie. Kolejność w pętli decyduje o tym, które zdjęcie jest \"na wierzchu\" (ostatnie będzie najwyżej). Jeśli chcesz, by środkowe było zawsze na wierzchu, trzeba by zmienić kolejność rysowania, ale przy 3 zdjęciach ten układ (lewe -> środkowe -> prawe) wygląda naturalnie."
            )
        )
    }
}