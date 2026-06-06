# Raport z prac nad projektem "Paths"

## Wykonane zadania:
1. **Konfiguracja Firebase**: 
   - Dodano biblioteki Firestore i Auth.
   - Skonfigurowano wtyczkę Google Services.
   - Poprawiono `Web Client ID` dla logowania Google.
2. **Model Danych**: 
   - Zaktualizowano klasę `Item.kt` o pola `type`, `ownerId` oraz poprawiono obsługę ocen (rezygnacja z typu Pair na rzecz osobnych pól).
3. **Logika Biznesowa**:
   - `ItemViewModel` obsługuje teraz nasłuchiwanie na żywo kolekcji `remoteRoutes` z filtrowaniem po typie trasy.
   - `AuthViewModel` zarządza stanem zalogowanego użytkownika oraz przechowuje preferencje filtrów.
4. **Interfejs Użytkownika**:
   - Wdrożono `HorizontalPager` do nawigacji gestami (Swipe).
   - Stworzono ekran profilu z logowaniem Google.
   - Stworzono panel filtrów (suwak odległości, ocena gwiazdkowa, chipy "Moje/Polubione").
   - Dodano przycisk "Do góry" w widoku szczegółów, resetujący widok do ekranu głównego.
