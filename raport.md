# Raport z prac nad projektem "Paths"

## Ogólne wyjaśnienie plików w pakiecie `com.example.paths`

*   **`MainActivity.kt`**: Główna aktywność aplikacji, inicjalizująca interfejs użytkownika w Compose.
    *   `onCreate`: Ustawia widok aplikacji i inicjalizuje UI.
*   **`Components.kt`**: Główny plik z komponentami UI (Composable). Zawiera nawigację i główne ekrany.
    *   `AppNavigation`: Zarządza nawigacją między ekranami aplikacji.
    *   `MainScreenContent`: Główny kontener interfejsu użytkownika.
    *   `ProfileScreen`: Ekran profilu użytkownika z opcjami logowania i ustawieniami.
    *   `FilterScreen`: Ekran filtrów wyszukiwania tras i punktów.
    *   `MainListContent`: Wyświetla listę elementów (szlaków i punktów).
    *   `ButtonField`: Komponent z przyciskami funkcyjnymi.
    *   `PhotoFan`: Animowany komponent do wyświetlania galerii zdjęć.
    *   `ItemRow`: Definiuje wygląd pojedynczego elementu na liście.
    *   `DetailPagerScreen`: Ekran szczegółów elementu wykorzystujący Pager.
    *   `DetailContent`: Wyświetla szczegółowe informacje o wybranym elemencie.
    *   `FullScreenImagePreview`: Pełnoekranowy podgląd wybranego zdjęcia.
    *   `AddRouteScreen`: Formularz dodawania nowej trasy z interaktywną mapą i przesyłaniem zdjęć.
*   **`AddRouteViewModel.kt`**: Zarządza stanem formularza dodawania nowej trasy, zapewniając trwałość danych przy rotacji ekranu.
    *   `name`, `shortDesc`, `longDesc`: Przechowują tekst wprowadzony w polach formularza.
    *   `isRower`: Określa wybrany typ trasy (rowerowa/piesza).
    *   `selectedImages`: Lista wybranych obrazów (URI).
    *   `pathPoints`: Lista współrzędnych `LatLng` zaznaczonych na mapie.
    *   `isMapFullScreen`: Stan określający, czy mapa jest wyświetlana w trybie pełnoekranowym.
    *   `clearForm`: Resetuje wszystkie pola formularza do wartości domyślnych.
    *   `getNewItem`: Tworzy obiekt klasy `Item` na podstawie aktualnego stanu formularza.
*   **`Item.kt`**: Modele danych używane w aplikacji.
    *   `RoutePoint`: Klasa reprezentująca punkt geograficzny.
    *   `Item`: Główna klasa danych reprezentująca szlak lub punkt zainteresowania.
*   **`ItemViewModel.kt`**: Zarządza danymi o elementach i komunikacją z Firebase Firestore.
    *   `setFilter`: Pozwala na filtrowanie elementów po typie.
    *   `fetchItems`: Pobiera i nasłuchuje na zmiany danych w bazie Firestore.
*   **`AuthViewModel.kt`**: Zarządza uwierzytelnianiem użytkownika oraz stanem filtrów i ustawień.
    *   `signInWithGoogle`: Realizuje proces logowania przez konto Google.
    *   `mockLogin`/`logout`: Obsługa sesji użytkownika.
    *   `setDarkMode`/`setButtonColor`: Zarządzanie motywem i kolorystyką przycisków.
    *   Funkcje typu `toggleOnlyMine`, `setMinRating`: Obsługa preferencji filtrowania.
*   **`StoperViewModel.kt`**: Zawiera logikę biznesową stopera mierzącego czas przejścia trasy.
    *   `start`: Rozpoczyna odliczanie czasu dla konkretnej trasy lub wznawia liczenie.
    *   `stop`: Zatrzymuje działanie stopera, resetuje czas do zera i (jeśli mierzono trasę) przygotowuje rekord do zapisu.
    *   `pause`: Wstrzymuje odliczanie czasu bez resetowania stanu i bez wywoływania okna zapisu.
    *   `reset`: Całkowicie zeruje stan stopera i aktywnej trasy.
*   **`Stopwatch.kt`**: Komponent UI odpowiedzialny za wyświetlanie stopera.
    *   `Stopwatch`: Composable renderujący interfejs graficzny stopera.
*   **`TimeUtils.kt`**: Funkcje pomocnicze do operacji na czasie.
    *   `formatTime`: Formatuje czas z milisekund do formatu tekstowego HH:MM:SS:mm.
*   **`LocalStopwatchRecord.kt`**: Definicja lokalnej bazy danych Room dla rekordów stopera.
    *   `LocalStopwatchRecord`: Encja przechowująca czas przejazdu trasy.
    *   `StopwatchDao`: Interfejs dostępu do danych (Insert, Query).
    *   `AppDatabase`: Główna klasa bazy danych Room.
*   **`ui/theme/`**: Zawiera pliki definicji motywu aplikacji (Kolory, Typografia, Główny motyw).

## Szczegółowe wyjaśnienie Cloud Functions (`functions/src/index.ts`)

Głównym zadaniem funkcji jest automatyzacja obliczeń po stronie serwera, aby aplikacja mobilna nie musiała pobierać tysięcy ocen, by wyliczyć średnią.

*   **`calculateAverageRating`**: Funkcja typu `onDocumentCreated`, która nasłuchuje na pojawienie się nowego dokumentu w kolekcji `routeRatings`.
    *   **`routeRatings/{ratingId}`**: Ścieżka dokumentu. `{ratingId}` to wild-card (zmienna), która reprezentuje ID nowej oceny.
    *   **`event.data`**: Przechowuje snapshot nowo utworzonego dokumentu oceny.
    *   **`ratingData`**: Obiekt zawierający dane nowej oceny (pola `routeId` oraz `rating`).
    *   **`db.runTransaction`**: Kluczowy mechanizm zapewniający integralność danych. Transakcja gwarantuje, że jeśli w tym samym czasie dwie osoby dodadzą ocenę, średnia zostanie obliczona poprawnie (operacja odczytu i zapisu jest atomowa).
    *   **Zmienne wewnątrz transakcji**:
        *   **`oldAverage` / `oldTotal`**: Pobiera aktualne statystyki z dokumentu trasy w kolekcji `remoteRoutes`.
        *   **`newTotal`**: Zwiększa licznik ocen o 1 (`oldTotal + 1`).
        *   **`newAverage`**: Matematyczny wzór na nową średnią: `(SumaWszystkichOcen + NowaOcena) / NowaLiczbaOcen`.
    *   **`transaction.update`**: Zapisuje nową średnią, nową liczbę ocen oraz czas aktualizacji (`updatedAt`) z powrotem do dokumentu trasy.

## Wykonane zadania:
1. **Konfiguracja Firebase i Cloud Functions**: 
   - Dodano biblioteki Firestore i Auth.
   - Skonfigurowano wtyczkę Google Services.
   - Utworzono katalog `functions` z projektem Node.js/TypeScript.
   - Wdrożono trigger `calculateAverageRating`, który automatycznie przelicza średnią ocenę trasy po dodaniu nowej opinii do kolekcji `routeRatings`.
   - Skonfigurowano **Google Maps API Key** oraz dodano odciski palca **SHA-1** dla wielu stanowisk deweloperskich.
2. **Model Danych i Baza Lokalna**: 
   - Zaktualizowano klasę `Item.kt` o pola `totalRatings`, `averageRating` (Double) oraz `startLocation` (GeoPoint).
   - Dodano obsługę bazy danych **Room** do przechowywania lokalnych wyników stopera (`LocalStopwatchRecord`).
   - Skonfigurowano wtyczkę **KSP** do obsługi generowania kodu Room.
3. **Logika Biznesowa i Stoper**:
   - `ItemViewModel` obsługuje teraz nasłuchiwanie na żywo kolekcji `remoteRoutes` z filtrowaniem po typie trasy.
   - `AuthViewModel` zarządza stanem zalogowanego użytkownika oraz przechowuje preferencje filtrów i motywu.
   - **Nowa logika stopera**: Wprowadzono funkcję `pause` (wstrzymanie) oraz poprawiono działanie `stop`, który teraz zawsze resetuje licznik i wywołuje zapis rekordu dla aktualnie mierzonej trasy, nawet po powrocie do menu głównego.
4. **Interfejs Użytkownika i Material 3**:
   - **Migracja do Material 3**: Zaktualizowano komponenty do standardu Material Design 3.
   - **Dynamiczne kolory**: Przyciski wyboru kategorii ("Rowerzysta", "Pieszy") korzystają teraz z `OutlinedIconButton` i dynamicznie zmieniają kolory (`primaryContainer`) po wybraniu.
   - **Reorganizacja układu**:
      - Przycisk dodawania trasy (+) został przeniesiony do dolnego paska jako **Floating Action Button**.
      - Panel sterowania został uproszczony i skupiony na obsłudze stopera (Play/Pause, Stop).
      - **Zegar systemowy**: Gdy stoper nie pracuje, wyświetla aktualną godzinę zamiast zer.
   - Wdrożono `HorizontalPager` do nawigacji gestami (Swipe).
   - Stworzono ekran profilu z logowaniem Google.
   - Stworzono panel filtrów (suwak odległości, ocena gwiazdkowa, chipy "Moje/Polubione").
   - Dodano przycisk "Do góry" w widoku szczegółów, resetujący widok do ekranu głównego.
   - Wdrożono ekran **Dodawania Trasy** (`AddRouteScreen`):
      - Obsługa wielu pól tekstowych z licznikami znaków.
      - Integracja z systemową galerią (do 10 zdjęć).
      - Interaktywna **Mapa Google**: pozwala na dodawanie punktów trasy poprzez kliknięcie, automatycznie rysuje linię (Polyline).
      - Przycisk "Opublikuj" z wskaźnikiem postępu (CircularProgressIndicator).
      - **Zoptymalizowano obsługę stanu**: Wprowadzono `AddRouteViewModel`, dzięki któremu dane formularza (teksty, zdjęcia, punkty na mapie) nie giną przy obracaniu telefonu.
      - **Poprawki UI mapy**: Przeniesiono przyciski sterujące (FullScreen) na lewą stronę, aby nie kolidowały z systemowym przyciskiem lokalizacji Google.
      - **Poprawa wydajności**: Zastosowano `rememberUpdatedMarkerState` dla znaczników mapy oraz `mutableFloatStateOf` dla transformacji obrazów.

## Rozwój systemu Map i Nawigacji (Plany)

Analiza dostępnych technologii Google Maps pod kątem aplikacji biegowej:
*   **Wizualizacja 3D**: Rozważane użycie `tilt` kamery oraz `Advanced Markers` dla lepszej czytelności terenu w miastach.
*   **Navigation SDK**: Rozpatrywana możliwość wdrożenia prowadzenia głosowego po śladzie GPS (opcja premium).
*   **Directions API**: Planowane wdrożenie automatycznego wyznaczania trasy między zaznaczonymi punktami (snap-to-roads).
*   **3D Terrain**: Włączenie widoku topograficznego dla biegów górskich.
