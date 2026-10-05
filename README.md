# LibraryApp

A small Kotlin/JVM console application for managing a library. Users can browse books, magazines and DVDs, search by title, borrow and return items (late returns are charged a fee), view statistics, and load new arrivals from a simulated server using coroutines.

## How to run

Requirements: JDK 17 or newer.

**From IntelliJ IDEA / Android Studio:** open the project folder, wait for Gradle sync, open `src/main/kotlin/Main.kt` and click the green Run arrow next to `fun main()`.

**From the terminal:**

```bash
# Windows
gradlew.bat run --console=plain

# macOS / Linux
./gradlew run --console=plain
```

Then choose menu options by typing a number and pressing Enter.

## Where the requirements are demonstrated

| Requirement | Where |
|---|---|
| Variables, data types, conditions, loops | `Main.kt`: `var running` + `while` menu loop, `when`, `for` in `printItems`; `Int`, `Double`, `String`, `Boolean` across the project; `if` checks in `Library.borrow()` / `returnItem()` |
| List, Set, Map | `Library.kt`: `items` (List), `members` / `loans` / `fines` (Map), `authors()` returns a Set |
| map, filter, reduce | `Library.totalPages()` (`map` + `reduce`), `Library.findItems()` (`filter`), `showStatistics()` in `Main.kt` (`map`, `count`, `groupBy`) |
| Functions, higher-order functions, lambdas | `Library.findItems(predicate: (LibraryItem) -> Boolean)`, `printItems(..., formatter: (LibraryItem) -> String)`; lambdas like `{ it.isAvailable }` in `Main.kt` |
| Classes and objects | `Library`, `Book`, `Magazine`, `Dvd`; objects created in `createLibrary()`; `companion object` in `Library` |
| Inheritance | `Models.kt`: abstract `LibraryItem` is extended by `Book`, `Magazine`, `Dvd` |
| Interfaces and polymorphism | `Describable` and `Borrowable` interfaces; each item overrides `describe()` and `lateFee()`, and `Library.returnItem()` calls `lateFee()` without knowing the concrete type |
| Data class | `Member`, `Loan` in `Models.kt` |
| Sealed class | `LibraryResult` (`Success`, `Error`, `NotFound`), handled with an exhaustive `when` in `printResult()` |
| Suspend function and coroutine | `Library.fetchNewArrivals()` is `suspend` (uses `delay`); `loadNewArrivals()` in `Main.kt` uses `runBlocking`, `async` and `launch` |

Coroutines dependency (`kotlinx-coroutines-core`) is added in `build.gradle.kts`.
