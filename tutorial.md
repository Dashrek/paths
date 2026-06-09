# Tutoriale projektowe

## Jak uruchomić i wdrożyć Cloud Functions (Triggery)?

1.  **Instalacja środowiska**:
    *   Upewnij się, że masz zainstalowany [Node.js](https://nodejs.org/).
    *   W terminalu zainstaluj Firebase CLI: `npm install -g firebase-tools`.
2.  **Przygotowanie folderu**:
    *   Przejdź do katalogu `functions` w projekcie: `cd functions`.
    *   Zainstaluj zależności: `npm install`.
3.  **Wdrożenie na serwer**:
    *   Zaloguj się: `firebase login`.
    *   Wybierz projekt: `firebase use fir-ofpaths`.
    *   Wyślij funkcje: `firebase deploy --only functions`.
4.  **Monitorowanie**:
    *   Logi funkcji znajdziesz w konsoli Firebase: **Functions > Logs**.

## Jak korzystać z bazy Room w projekcie?

1.  **Generowanie kodu**: Po każdej zmianie w `LocalStopwatchRecord.kt` (np. dodaniu pola do encji), wykonaj **Build > Rebuild Project**, aby wtyczka KSP wygenerowała nowe klasy pomocnicze.
2.  **Podgląd bazy na żywo**:
    *   Uruchom aplikację na emulatorze.
    *   W Android Studio otwórz zakładkę **App Inspection** (zazwyczaj na dole).
    *   Wybierz zakładkę **Database Inspector**, aby widzieć tabelę `stopwatch_records` i wykonywać zapytania SQL na żywo.

## Czy muszę aktualizować `google-services.json`?

**Nie**, jeśli zmieniasz tylko strukturę bazy danych (dodajesz kolekcje, pola czy GeoPointy). Plik ten zawiera dane dostępowe do projektu, które pozostają niezmienne. Aplikacja pobiera nową strukturę danych dynamicznie przez SDK Firebase.

## Jak korzystać ze stopera i zapisywać rekordy?

1.  **Rozpoczęcie pomiaru**: Kliknij ikonę Play (`>`) w panelu głównym lub bezpośrednio przy wybranej trasie.
2.  **Wstrzymanie (Pause)**: Kliknij ikonę `||`, aby wstrzymać czas (np. na odpoczynek). Ikona zmieni się z powrotem na `>`, a czas zostanie zachowany.
3.  **Zatrzymanie i Zapis (Stop)**: Kliknij ikonę kwadratu (`■`). 
    *   Stoper zostanie zresetowany do zera.
    *   Jeśli mierzyłeś czas konkretnej trasy, pojawi się okno z pytaniem o zapisanie rekordu.
    *   Rekordy are zapisywane lokalnie w bazie Room i wyświetlane w szczegółach trasy.
4.  **Zegar systemowy**: Gdy stoper nie jest używany, wyświetla aktualną godzinę systemową.

## Jak zmienić wygląd przycisków i motyw (Material 3)?

Aplikacja w pełni wykorzystuje Material Design 3, co pozwala na dynamiczne zarządzanie wyglądem:

1.  **Przełączanie motywu**: W zakładce "Ustawienia" (FilterScreen) możesz ręcznie włączyć/wyłączyć tryb ciemny.
2.  **Wybór kategorii**: Przyciski "Rowerzysta" i "Pieszy" to `OutlinedIconButton`. Po kliknięciu zmieniają one kolor tła na `primaryContainer` oraz obramowanie na `primary`.
3.  **Dodawanie tras**: Przycisk dodawania trasy jest dostępny jako `FloatingActionButton` (+) na dole ekranu głównego.

## Jak edytować listy (LazyColumn/LazyRow) i siatki (LazyVerticalGrid)?

W projekcie używamy komponentów `Lazy`, które są wydajne i renderują tylko to, co widać na ekranie.

### 1. Edycja Listy (LazyColumn / LazyRow)
Jeśli chcesz zmienić sposób wyświetlania elementów na liście (np. w galerii zdjęć lub rekordach):
*   **Zmiana układu**: Użyj `items(lista) { element -> ... }` wewnątrz bloku `Lazy`.
*   **Odstępy**: Parametr `horizontalArrangement = Arrangement.spacedBy(8.dp)` w `LazyRow` dodaje równe odstępy między elementami.
*   **Padding**: Zamiast `Modifier.padding`, użyj `contentPadding = PaddingValues(...)`, aby lista nie była ucinana przy krawędziach podczas przewijania.

### 2. Edycja Siatki (LazyVerticalGrid)
Siatki są używane tam, gdzie potrzebujesz wielu kolumn (np. wybór zdjęć):
*   **Liczba kolumn**: `columns = GridCells.Fixed(3)` wymusza 3 kolumny. `GridCells.Adaptive(128.dp)` sam dobierze liczbę kolumn zależnie od szerokości ekranu.
*   **Elementy**: Używamy `gridItemsIndexed` (alias w `Components.kt`), aby mieć dostęp do indeksu elementu.

**Przykład dodania ramki do wybranego zdjęcia w siatce:**
```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(3)) {
    items(images) { uri ->
        Box(modifier = Modifier
            .border(2.dp, if(isSelected) Color.Orange else Color.Transparent)
        ) {
            AsyncImage(model = uri, ...)
        }
    }
}
```

## Jak działają dynamiczne kolory i motyw?

System motywów został przebudowany, aby reagować na wybór użytkownika:

1.  **Stan w AuthViewModel**: Przechowuje `buttonColor` (jako Long) oraz `isDarkMode` (Boolean?).
2.  **MainActivity**: Pobiera te wartości i przekazuje je do `PathsTheme`.
3.  **PathsTheme (Theme.kt)**: Przyjmuje parametr `primaryColor`. Jeśli jest podany, nadpisuje domyślny kolor `primary` w schemacie kolorów (Light/Dark).
4.  **Zastosowanie**: Dzięki temu po kliknięciu koloru w ustawieniach, cała aplikacja (przyciski, zaznaczenia, ikony `primary`) zmienia barwę natychmiastowo.

## Jak wygenerować i dodać klucz API Map Google?

Aby mapa w formularzu dodawania trasy działała poprawnie, musisz posiadać własny klucz API:

1.  Wejdź do [Google Cloud Console](https://console.cloud.google.com/).
2.  Wybierz swój projekt `fir-ofpaths`.
3.  Przejdź do **APIs & Services > Library**.
4.  Wyszukaj i włącz **Maps SDK for Android**.
5.  Przejdź do **APIs & Services > Credentials**.
6.  Kliknij **+ CREATE CREDENTIALS > API key**.
7.  Skopiuj wygenerowany klucz.
8.  Otwórz plik `app/src/main/AndroidManifest.xml` in Android Studio.
9.  Znajdź sekcję `<meta-data android:name="com.google.android.geo.API_KEY" ... />`.
10. Wklej swój klucz w miejsce `TWÓJ_KLUCZ_API_TUTAJ`.
11. (Zalecane) Wróć do konsoli Google Cloud i kliknij "Edit API key", aby nałożyć restrykcje (wybrać tylko "Android apps" i podać swój pakiet `com.example.paths` oraz SHA-1), aby nikt inny nie mógł korzystać z Twojego klucza.

## Jak dodać własne przyciski i kontrolki do Mapy Google?

Standardowe przyciski Google Maps (lokalizacja, zoom) znajdują się zazwyczaj po prawej stronie. Aby dodać własne kontrolki (np. przełącznik trybu pełnoekranowego, reset punktów):

1. **Użyj kontenera `Box`**: Umieść `GoogleMap` oraz przyciski wewnątrz `Box`, aby móc nakładać elementy na siebie.
2. **Pozycjonowanie**: Użyj `Modifier.align(Alignment...)`, aby umieścić przycisk w wybranym rogu. Zaleca się używanie `TopStart` (lewy górny róg) lub `BottomStart`, aby uniknąć nakładania się na systemowy przycisk lokalizacji (`TopEnd`).
3. **Stylizacja**: Dodaj tło (np. półprzezroczysty czarny) i kształt (`CircleShape`), aby przycisk był widoczny na każdym typie mapy.

**Przykład kodu:**
```kotlin
Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        // konfiguracja mapy...
    )

    // Własny przycisk w lewym górnym rogu
    IconButton(
        onClick = { /* akcja */ },
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
    ) {
        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White)
    }
}
```
