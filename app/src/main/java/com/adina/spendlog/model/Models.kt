package com.adina.spendlog.model

interface Describable {
    fun describe(): String
}

interface Balanceable {
    val canGoNegative: Boolean
    val creditLimit: Double
    fun maxSpendable(currentBalance: Double): Double {
        return if (canGoNegative) currentBalance + creditLimit else currentBalance
    }
}

abstract class Account(
    val id: Int,
    val name: String,
    val startingBalance: Double
) : Describable, Balanceable {

    abstract val type: String

    override fun describe(): String = "[$id] $type: \"$name\" (start ${"%.2f".format(startingBalance)})"
}

class CashAccount(
    id: Int,
    name: String,
    startingBalance: Double
) : Account(id, name, startingBalance) {
    override val type = "Cash"
    override val canGoNegative = false
    override val creditLimit = 0.0
}

class CardAccount(
    id: Int,
    name: String,
    startingBalance: Double,
    override val creditLimit: Double
) : Account(id, name, startingBalance) {
    override val type = "Card"
    override val canGoNegative = true
    override fun describe(): String =
        super.describe() + ", credit limit ${"%.2f".format(creditLimit)}"
}

class SavingsAccount(
    id: Int,
    name: String,
    startingBalance: Double
) : Account(id, name, startingBalance) {
    override val type = "Savings"
    override val canGoNegative = false
    override val creditLimit = 0.0
}

abstract class Transaction(
    val id: Int,
    val accountId: Int,
    val category: String,
    val amount: Double,
    val note: String,
    val timestampMillis: Long
) : Describable {

    abstract val kind: String

    override fun describe(): String =
        "[$id] $kind: ${"%.2f".format(amount)} · $category — $note"
}

class Expense(
    id: Int,
    accountId: Int,
    category: String,
    amount: Double,
    note: String,
    timestampMillis: Long
) : Transaction(id, accountId, category, amount, note, timestampMillis) {
    override val kind = "Expense"
}

class Income(
    id: Int,
    accountId: Int,
    category: String,
    amount: Double,
    note: String,
    timestampMillis: Long
) : Transaction(id, accountId, category, amount, note, timestampMillis) {
    override val kind = "Income"
}

data class Budget(val category: String, val monthlyLimit: Double)

data class AccountBalance(
    val account: Account,
    val balance: Double
)

data class CategorySpend(
    val category: String,
    val spent: Double,
    val limit: Double?
) {
    val progress: Float
        get() {
            val cap = limit ?: return 0f
            if (cap <= 0) return 0f
            return (spent / cap).toFloat().coerceAtMost(1.5f)
        }

    val isOverBudget: Boolean = limit != null && spent > limit
}

sealed class OperationResult {
    data class Success(val message: String) : OperationResult()
    data class Error(val reason: String) : OperationResult()
    data object NotFound : OperationResult()
}

object Categories {
    val expense = listOf("Food", "Transport", "Study", "Fun", "Health", "Other")
    val income = listOf("Salary", "Gift", "Other")
    val all = (expense + income).distinct()
}
