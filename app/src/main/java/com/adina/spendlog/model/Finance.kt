package com.adina.spendlog.model

import kotlinx.coroutines.delay
import java.util.Calendar

class Finance(val ownerName: String) {

    private val accounts = mutableMapOf<Int, Account>()
    private val transactions = mutableListOf<Transaction>()
    private val budgets = mutableMapOf<String, Budget>()

    fun addAccount(account: Account) {
        accounts[account.id] = account
    }

    fun addBudget(budget: Budget) {
        budgets[budget.category] = budget
    }

    fun allAccounts(): List<Account> = accounts.values.sortedBy { it.id }

    fun allTransactions(): List<Transaction> =
        transactions.sortedByDescending { it.timestampMillis }

    fun allBudgets(): List<Budget> = budgets.values.sortedBy { it.category }

    fun findTransaction(id: Int): Transaction? = transactions.find { it.id == id }

    fun balanceFor(accountId: Int): Double {
        val account = accounts[accountId] ?: return 0.0
        val delta = transactions
            .filter { it.accountId == accountId }
            .sumOf { tx ->
                when (tx) {
                    is Income -> tx.amount
                    is Expense -> -tx.amount
                    else -> 0.0
                }
            }
        return account.startingBalance + delta
    }

    fun balances(): List<AccountBalance> =
        allAccounts().map { AccountBalance(it, balanceFor(it.id)) }

    fun totalIncome(): Double = transactions.filterIsInstance<Income>().sumOf { it.amount }

    fun totalExpense(): Double = transactions.filterIsInstance<Expense>().sumOf { it.amount }

    fun spentByCategory(): List<CategorySpend> {
        val spent = transactions
            .filterIsInstance<Expense>()
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val categories = (spent.keys + budgets.keys).distinct().sorted()
        return categories.map { category ->
            CategorySpend(
                category = category,
                spent = spent[category] ?: 0.0,
                limit = budgets[category]?.monthlyLimit
            )
        }
    }

    fun addTransaction(
        kind: String,
        accountId: Int,
        category: String,
        amount: Double,
        note: String,
        timestampMillis: Long = System.currentTimeMillis()
    ): OperationResult {
        val account = accounts[accountId]
            ?: return OperationResult.Error("Account #$accountId does not exist")
        if (amount <= 0) {
            return OperationResult.Error("Amount must be greater than zero")
        }
        if (category.isBlank()) {
            return OperationResult.Error("Category is required")
        }
        if (note.isBlank()) {
            return OperationResult.Error("Write a short note about what you did")
        }

        if (kind == "Expense") {
            val balance = balanceFor(accountId)
            val maxSpendable = account.maxSpendable(balance)
            if (amount > maxSpendable) {
                return OperationResult.Error(
                    "Not enough money on \"${account.name}\". " +
                        "Available: ${"%.2f".format(maxSpendable)}"
                )
            }
        }

        val nextId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
        val tx = when (kind) {
            "Income" -> Income(nextId, accountId, category, amount, note.trim(), timestampMillis)
            else -> Expense(nextId, accountId, category, amount, note.trim(), timestampMillis)
        }
        transactions.add(tx)

        val budgetWarning = if (tx is Expense) {
            val budget = budgets[tx.category]
            if (budget != null) {
                val spent = transactions
                    .filterIsInstance<Expense>()
                    .filter { it.category == tx.category }
                    .sumOf { it.amount }
                if (spent > budget.monthlyLimit) {
                    " Warning: \"$category\" is over budget " +
                        "(${"%.2f".format(spent)} / ${"%.2f".format(budget.monthlyLimit)})."
                } else {
                    ""
                }
            } else {
                ""
            }
        } else {
            ""
        }

        return OperationResult.Success(
            "${tx.kind} ${"%.2f".format(tx.amount)} logged · ${tx.note}$budgetWarning"
        )
    }

    fun addAccount(
        type: String,
        name: String,
        startingBalance: Double,
        creditLimit: Double
    ): OperationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return OperationResult.Error("Account name is required")
        }
        if (accounts.values.any { it.name.equals(trimmed, ignoreCase = true) }) {
            return OperationResult.Error("An account named \"$trimmed\" already exists")
        }
        if (startingBalance < 0 && type != "Card") {
            return OperationResult.Error("Only a card can start with a negative balance")
        }

        val nextId = (accounts.keys.maxOrNull() ?: 0) + 1
        val account = when (type) {
            "Card" -> CardAccount(nextId, trimmed, startingBalance, creditLimit.coerceAtLeast(0.0))
            "Savings" -> SavingsAccount(nextId, trimmed, startingBalance.coerceAtLeast(0.0))
            else -> CashAccount(nextId, trimmed, startingBalance.coerceAtLeast(0.0))
        }
        accounts[account.id] = account
        return OperationResult.Success("Account \"${account.name}\" added")
    }

    suspend fun fetchDemoMonth(): List<Transaction> {
        delay(1500)
        val cashId = accounts.values.find { it is CashAccount }?.id
            ?: accounts.keys.firstOrNull()
            ?: return emptyList()
        val cardId = accounts.values.find { it is CardAccount }?.id ?: cashId
        val firstId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        return listOf(
            Expense(firstId, cardId, "Food", 1800.0, "кофе с Алией после пары", now - day),
            Expense(firstId + 1, cashId, "Transport", 400.0, "автобус до универа", now - 2 * day),
            Expense(firstId + 2, cardId, "Study", 3500.0, "распечатала конспекты", now - 3 * day),
            Income(firstId + 3, cardId, "Salary", 45000.0, "стипендия за месяц", now - 5 * day),
            Expense(firstId + 4, cardId, "Fun", 5200.0, "кино в выходные", now - 6 * day)
        )
    }

    fun monthLabel(): String {
        val cal = Calendar.getInstance()
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
    }

    companion object {
        const val MAX_ACCOUNTS = 8
    }
}
