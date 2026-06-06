# Tutoriale projektowe

## Jak dodać kolejny szablon przycisku?

Aby dodać nowy styl przycisku do listy wyborów:

1. **Definicja koloru/stylu**: W `AuthViewModel.kt` wewnątrz klasy (lub jako stała) dodaj nowy element do listy szablonów. Każdy szablon powinien definiować `containerColor` oraz `contentColor`.
   ```kotlin
   data class ButtonTemplate(val name: String, val color: Color)
   ```
2. **Aktualizacja listy**: Dodaj nowy obiekt `ButtonTemplate` do listy, którą wyświetla `DropdownMenu`.
3. **Zastosowanie**: W komponencie `MainListContent`, w miejscu gdzie tworzysz przyciski, użyj wartości z ViewModelu:
   ```kotlin
   colors = ButtonDefaults.buttonColors(
       containerColor = selectedTemplate.color
   )
   ```
4. **Reakcja**: Pamiętaj, aby funkcja `onValueChange` w rozwijanej liście aktualizowała stan w ViewModelu.
