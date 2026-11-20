package com.adina.spendlog.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

object FinanceRules {

    const val MAX_ACCOUNTS = 8
    const val MAX_NOTE_LENGTH = 200
    const val MAX_NAME_LENGTH = 30
    const val MAX_AMOUNT = 100_000_000.0
    const val NEAR_LIMIT_RATIO = 0.8f

    fun balanceFor(account: Account, transactions: List<Transaction>): Double =
        account.startingBalance + transactions
            .filter { it.accountId == account.id }
            .sumOf { it.signedAmount }

    fun balances(accounts: List<Account>, transactions: List<Transaction>): List<AccountBalance> =
        accounts.map { AccountBalance(it, balanceFor(it, transactions)) }

    fun firstOverdrawn(
        accounts: List<Account>,
        transactions: List<Transaction>,
        affectedAccountIds: Set<Int>
    ): AccountBalance? =
        balances(accounts.filter { it.id in affectedAccountIds }, transactions)
            .firstOrNull { it.balance < it.account.lowestAllowedBalance() - 0.001 }

    fun overdraftMessage(balance: AccountBalance): String {
        val account = balance.account
        return if (account.canGoNegative) {
            "\"${account.name}\" would go over its credit limit of ${formatMoney(account.creditLimit)}"
        } else {
            "Not enough money on \"${account.name}\": ${account.type} can't go below 0 " +
                "(would be ${formatMoney(balance.balance)})"
        }
    }

    fun currentMonthRange(nowMillis: Long = System.currentTimeMillis()): LongRange {
        val start = startOfMonth(nowMillis)
        val end = Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.MONTH, 1)
        }.timeInMillis
        return start until end
    }

    fun spentByCategory(monthTransactions: List<Transaction>, budgets: List<Budget>): List<CategorySpend> {
        val spent = monthTransactions
            .filterIsInstance<Expense>()
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val limits = budgets.associate { it.category to it.monthlyLimit }

        return (spent.keys + limits.keys)
            .distinct()
            .map { CategorySpend(it, spent[it] ?: 0.0, limits[it]) }
            .sortedWith(compareByDescending<CategorySpend> { it.hasBudget }.thenByDescending { it.spent })
    }

    fun budgetWarning(
        category: String,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        nowMillis: Long = System.currentTimeMillis()
    ): String? {
        val budget = budgets.find { it.category == category } ?: return null
        val month = currentMonthRange(nowMillis)
        val spent = transactions
            .filterIsInstance<Expense>()
            .filter { it.category == category && it.timestampMillis in month }
            .sumOf { it.amount }
        return when {
            spent > budget.monthlyLimit ->
                "Over \"$category\" budget by ${formatMoney(spent - budget.monthlyLimit)}"
            spent >= budget.monthlyLimit * NEAR_LIMIT_RATIO ->
                "${(spent / budget.monthlyLimit * 100).roundToInt()}% of \"$category\" budget used"
            else -> null
        }
    }

    fun monthlyHistory(
        transactions: List<Transaction>,
        months: Int = 6,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MonthSummary> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = startOfMonth(nowMillis)
            add(Calendar.MONTH, -(months - 1))
        }
        val labelFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        return List(months) {
            val start = cal.timeInMillis
            val label = labelFormat.format(cal.time)
            cal.add(Calendar.MONTH, 1)
            val inMonth = transactions.filter { it.timestampMillis in start until cal.timeInMillis }
            MonthSummary(
                label = label,
                income = inMonth.filterIsInstance<Income>().sumOf { it.amount },
                expense = inMonth.filterIsInstance<Expense>().sumOf { it.amount }
            )
        }
    }

    fun daysLeftInMonth(nowMillis: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH)
    }

    fun monthLabel(nowMillis: Long = System.currentTimeMillis()): String =
        SimpleDateFormat("LLLL yyyy", Locale.ENGLISH).format(nowMillis)

    private fun startOfMonth(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun formatMoney(value: Double): String = String.format(Locale.US, "%,.0f ₸", value)

fun formatMoneyPrecise(value: Double): String = String.format(Locale.US, "%,.2f ₸", value)

fun formatAmountInput(value: Double): String =
    String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
