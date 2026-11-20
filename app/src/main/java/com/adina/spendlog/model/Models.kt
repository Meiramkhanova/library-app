package com.adina.spendlog.model

interface Describable {
    fun describe(): String
}

interface Balanceable {
    val canGoNegative: Boolean
    val creditLimit: Double

    fun maxSpendable(currentBalance: Double): Double =
        if (canGoNegative) currentBalance + creditLimit else currentBalance

    fun lowestAllowedBalance(): Double = if (canGoNegative) -creditLimit else 0.0
}

abstract class Account(
    val id: Int,
    val name: String,
    val startingBalance: Double
) : Describable, Balanceable {

    abstract val type: String

    override fun describe(): String = "[$id] $type: \"$name\" (start ${formatMoney(startingBalance)})"
}

class CashAccount(
    id: Int,
    name: String,
    startingBalance: Double
) : Account(id, name, startingBalance) {
    override val type = AccountType.CASH
    override val canGoNegative = false
    override val creditLimit = 0.0
}

class CardAccount(
    id: Int,
    name: String,
    startingBalance: Double,
    override val creditLimit: Double
) : Account(id, name, startingBalance) {
    override val type = AccountType.CARD
    override val canGoNegative = true
    override fun describe(): String = super.describe() + ", credit limit ${formatMoney(creditLimit)}"
}

class SavingsAccount(
    id: Int,
    name: String,
    startingBalance: Double
) : Account(id, name, startingBalance) {
    override val type = AccountType.SAVINGS
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
    abstract val signedAmount: Double

    override fun describe(): String = "[$id] $kind: ${formatMoney(amount)} · $category — $note"
}

class Expense(
    id: Int,
    accountId: Int,
    category: String,
    amount: Double,
    note: String,
    timestampMillis: Long
) : Transaction(id, accountId, category, amount, note, timestampMillis) {
    override val kind = TransactionKind.EXPENSE
    override val signedAmount: Double get() = -amount
}

class Income(
    id: Int,
    accountId: Int,
    category: String,
    amount: Double,
    note: String,
    timestampMillis: Long
) : Transaction(id, accountId, category, amount, note, timestampMillis) {
    override val kind = TransactionKind.INCOME
    override val signedAmount: Double get() = amount
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
    val hasBudget: Boolean get() = limit != null

    val progress: Float
        get() {
            val cap = limit ?: return 0f
            if (cap <= 0) return 0f
            return (spent / cap).toFloat()
        }

    val isOverBudget: Boolean get() = limit != null && spent > limit

    val isNearLimit: Boolean
        get() = limit != null && !isOverBudget && progress >= FinanceRules.NEAR_LIMIT_RATIO

    val remaining: Double get() = (limit ?: 0.0) - spent
}

data class MonthSummary(
    val label: String,
    val income: Double,
    val expense: Double
) {
    val net: Double get() = income - expense
}

data class TransactionDraft(
    val id: Int?,
    val kind: String,
    val accountId: Int,
    val category: String,
    val amount: Double,
    val note: String,
    val timestampMillis: Long
)

data class AccountDraft(
    val id: Int?,
    val type: String,
    val name: String,
    val startingBalance: Double,
    val creditLimit: Double
)

sealed class OperationResult {
    data class Success(val message: String) : OperationResult()
    data class Error(val reason: String) : OperationResult()
    data object NotFound : OperationResult()
}

object TransactionKind {
    const val EXPENSE = "Expense"
    const val INCOME = "Income"
    val all = listOf(EXPENSE, INCOME)
}

object AccountType {
    const val CASH = "Cash"
    const val CARD = "Card"
    const val SAVINGS = "Savings"
    val all = listOf(CASH, CARD, SAVINGS)
}

object Categories {
    val expense = listOf("Food", "Transport", "Study", "Fun", "Health", "Other")
    val income = listOf("Salary", "Gift", "Other")

    fun forKind(kind: String): List<String> =
        if (kind == TransactionKind.INCOME) income else expense
}
