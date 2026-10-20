package com.adina.libraryapp.model

import kotlinx.coroutines.delay

class Library(val name: String) {

    private val items = mutableListOf<LibraryItem>()
    private val members = mutableMapOf<Int, Member>()
    private val loans = mutableMapOf<Int, Loan>()
    private val fines = mutableMapOf<Int, Double>()

    fun addItem(item: LibraryItem) {
        items.add(item)
    }

    fun addMember(member: Member) {
        members[member.id] = member
    }

    fun allItems(): List<LibraryItem> = items.toList()

    fun allMembers(): List<Member> = members.values.toList()

    fun findItems(predicate: (LibraryItem) -> Boolean): List<LibraryItem> = items.filter(predicate)

    fun borrow(itemId: Int, memberId: Int): LibraryResult {
        val item = items.find { it.id == itemId } ?: return LibraryResult.NotFound
        val member = members[memberId] ?: return LibraryResult.Error("Member #$memberId does not exist")

        if (!item.isAvailable) {
            return LibraryResult.Error("\"${item.title}\" is already borrowed")
        }

        val activeLoans = loans.values.count { it.member.id == memberId }
        if (activeLoans >= MAX_LOANS) {
            return LibraryResult.Error("${member.name} already has $MAX_LOANS items")
        }

        item.isAvailable = false
        loans[itemId] = Loan(item, member)
        return LibraryResult.Success("${member.name} borrowed \"${item.title}\" for ${item.loanDays} days")
    }

    fun returnItem(itemId: Int, daysKept: Int): LibraryResult {
        val loan = loans.remove(itemId) ?: return LibraryResult.NotFound
        loan.item.isAvailable = true

        val daysLate = daysKept - loan.item.loanDays
        if (daysLate <= 0) {
            return LibraryResult.Success("\"${loan.item.title}\" returned on time")
        }

        val fee = loan.item.lateFee(daysLate)
        fines[loan.member.id] = (fines[loan.member.id] ?: 0.0) + fee
        return LibraryResult.Success(
            "\"${loan.item.title}\" returned $daysLate days late. Fee: $%.2f".format(fee)
        )
    }

    fun activeLoans(): List<Loan> = loans.values.toList()

    fun authors(): Set<String> = items.filterIsInstance<Book>().map { it.author }.toSet()

    fun totalPages(): Int {
        val books = items.filterIsInstance<Book>()
        if (books.isEmpty()) return 0
        return books.map { it.pages }.reduce { sum, pages -> sum + pages }
    }

    fun countByType(): Map<String, Int> = items.groupBy { it.type }.mapValues { it.value.size }

    fun finesByMember(): Map<String, Double> =
        fines.mapKeys { (memberId, _) -> members[memberId]?.name ?: "Unknown" }

    suspend fun fetchNewArrivals(): List<LibraryItem> {
        delay(1500)
        val firstId = (items.maxOfOrNull { it.id } ?: 0) + 1
        return listOf(
            Book(firstId, "Kotlin in Action", 2017, "Dmitry Jemerov", 360),
            Magazine(firstId + 1, "Wired", 2024, 12),
            Dvd(firstId + 2, "The Matrix", 1999, 136)
        )
    }

    companion object {
        const val MAX_LOANS = 3
    }
}
