package com.adina.libraryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adina.libraryapp.model.Book
import com.adina.libraryapp.model.Dvd
import com.adina.libraryapp.model.Library
import com.adina.libraryapp.model.LibraryItem
import com.adina.libraryapp.model.LibraryResult
import com.adina.libraryapp.model.Loan
import com.adina.libraryapp.model.Magazine
import com.adina.libraryapp.model.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable snapshot of everything the UI needs.
 * [Library] mutates its own lists, so after every operation the ViewModel
 * re-reads the domain state and publishes a fresh snapshot through [StateFlow].
 */
data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val members: List<Member> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val finesByMember: Map<String, Double> = emptyMap(),
    val isLoadingArrivals: Boolean = false,
    val message: String? = null
)

/**
 * Owns the [Library] instance and all application logic.
 * Survives Activity recreation (e.g. rotation); the UI only renders [uiState]
 * and forwards user actions to the functions below.
 */
class LibraryViewModel : ViewModel() {

    private val library = Library("City Library")

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        seedLibrary()
        refresh()
    }

    private fun seedLibrary() {
        listOf(
            Book(1, "Clean Code", 2008, "Robert Martin", 464),
            Book(2, "Clean Architecture", 2017, "Robert Martin", 432),
            Book(3, "Effective Java", 2018, "Joshua Bloch", 412),
            Book(4, "The Pragmatic Programmer", 1999, "Andrew Hunt", 352)
        ).forEach { library.addItem(it) }

        library.addItem(Magazine(5, "National Geographic", 2023, 7))
        library.addItem(Dvd(6, "Interstellar", 2014, 169))

        library.addMember(Member(1, "Adina", "adina@mail.com"))
        library.addMember(Member(2, "Aliya", "aliya@mail.com"))
        library.addMember(Member(3, "Timur", "timur@mail.com"))
    }

    fun borrow(itemId: Int, memberId: Int) {
        refresh(message = library.borrow(itemId, memberId).toMessage())
    }

    fun returnItem(itemId: Int, daysKept: Int) {
        refresh(message = library.returnItem(itemId, daysKept).toMessage())
    }

    fun addItem(
        type: String,
        title: String,
        year: Int,
        author: String,
        pages: Int,
        issue: Int,
        durationMinutes: Int
    ) {
        val nextId = (library.allItems().maxOfOrNull { it.id } ?: 0) + 1
        val item = when (type) {
            "Magazine" -> Magazine(nextId, title, year, issue)
            "DVD" -> Dvd(nextId, title, year, durationMinutes)
            else -> Book(nextId, title, year, author, pages)
        }
        library.addItem(item)
        refresh(message = "Added \"${item.title}\"")
    }

    fun loadNewArrivals() {
        if (_uiState.value.isLoadingArrivals) return
        refresh(loading = true)
        viewModelScope.launch {
            val arrivals = library.fetchNewArrivals()
            arrivals.forEach { library.addItem(it) }
            refresh(message = "Added ${arrivals.size} new arrivals", loading = false)
        }
    }

    fun consumeMessage() {
        refresh(message = null)
    }

    private fun refresh(
        message: String? = _uiState.value.message,
        loading: Boolean = _uiState.value.isLoadingArrivals
    ) {
        _uiState.value = LibraryUiState(
            items = library.allItems(),
            members = library.allMembers(),
            loans = library.activeLoans(),
            finesByMember = library.finesByMember(),
            isLoadingArrivals = loading,
            message = message
        )
    }

    private fun LibraryResult.toMessage(): String = when (this) {
        is LibraryResult.Success -> message
        is LibraryResult.Error -> reason
        LibraryResult.NotFound -> "Item not found"
    }
}
