package com.adina.libraryapp.model

interface Describable {
    fun describe(): String
}

interface Borrowable {
    val loanDays: Int
    fun lateFee(daysLate: Int): Double
}

abstract class LibraryItem(
    val id: Int,
    val title: String,
    val year: Int
) : Describable, Borrowable {

    var isAvailable: Boolean = true
    abstract val type: String

    override fun describe(): String {
        val status = if (isAvailable) "available" else "borrowed"
        return "[$id] $type: \"$title\" ($year) - $status"
    }
}

class Book(
    id: Int,
    title: String,
    year: Int,
    val author: String,
    val pages: Int
) : LibraryItem(id, title, year) {
    override val type = "Book"
    override val loanDays = 14
    override fun lateFee(daysLate: Int): Double = daysLate * 0.5
    override fun describe(): String = super.describe() + ", by $author, $pages pages"
}

class Magazine(
    id: Int,
    title: String,
    year: Int,
    val issue: Int
) : LibraryItem(id, title, year) {
    override val type = "Magazine"
    override val loanDays = 7
    override fun lateFee(daysLate: Int): Double = daysLate * 0.25
    override fun describe(): String = super.describe() + ", issue #$issue"
}

class Dvd(
    id: Int,
    title: String,
    year: Int,
    val durationMinutes: Int
) : LibraryItem(id, title, year) {
    override val type = "DVD"
    override val loanDays = 3
    override fun lateFee(daysLate: Int): Double = daysLate * 1.0
    override fun describe(): String = super.describe() + ", $durationMinutes min"
}

data class Member(val id: Int, val name: String, val email: String)

data class Loan(val item: LibraryItem, val member: Member)

sealed class LibraryResult {
    data class Success(val message: String) : LibraryResult()
    data class Error(val reason: String) : LibraryResult()
    data object NotFound : LibraryResult()
}
