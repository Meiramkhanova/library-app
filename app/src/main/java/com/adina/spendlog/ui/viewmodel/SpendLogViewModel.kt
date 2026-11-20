package com.adina.spendlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adina.spendlog.data.repository.FinanceRepository
import com.adina.spendlog.model.AccountBalance
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.Budget
import com.adina.spendlog.model.Categories
import com.adina.spendlog.model.CategorySpend
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.FinanceRules
import com.adina.spendlog.model.Income
import com.adina.spendlog.model.MonthSummary
import com.adina.spendlog.model.OperationResult
import com.adina.spendlog.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserMessage(
    val id: Long,
    val text: String,
    val actionLabel: String? = null
)

data class SpendLogUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val balances: List<AccountBalance> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val categorySpend: List<CategorySpend> = emptyList(),
    val history: List<MonthSummary> = emptyList(),
    val monthTransactions: List<Transaction> = emptyList(),
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthLabel: String = "",
    val daysLeftInMonth: Int = 0,
    val isLoadingDemo: Boolean = false,
    val message: UserMessage? = null
) {
    val totalBalance: Double get() = balances.sumOf { it.balance }
    val overBudgetCount: Int get() = categorySpend.count { it.isOverBudget }
    val categoriesWithoutBudget: List<String>
        get() = Categories.expense.filter { category -> budgets.none { it.category == category } }

    fun accountName(accountId: Int): String =
        balances.find { it.account.id == accountId }?.account?.name ?: "Unknown account"

    fun transaction(id: Int): Transaction? = transactions.find { it.id == id }

    fun spendFor(category: String): CategorySpend? = categorySpend.find { it.category == category }
}

private data class TransientState(
    val isLoadingDemo: Boolean = false,
    val message: UserMessage? = null
)

class SpendLogViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val transient = MutableStateFlow(TransientState())
    private var lastDeleted: Transaction? = null
    private var nextMessageId = 0L

    val uiState: StateFlow<SpendLogUiState> = combine(
        repository.transactions,
        repository.accounts,
        repository.budgets,
        transient
    ) { transactions, accounts, budgets, state ->
        val now = System.currentTimeMillis()
        val month = FinanceRules.currentMonthRange(now)
        val monthTransactions = transactions.filter { it.timestampMillis in month }
        SpendLogUiState(
            isLoading = false,
            transactions = transactions,
            balances = FinanceRules.balances(accounts, transactions),
            budgets = budgets,
            categorySpend = FinanceRules.spentByCategory(monthTransactions, budgets),
            history = FinanceRules.monthlyHistory(transactions, months = 6, nowMillis = now),
            monthTransactions = monthTransactions,
            monthIncome = monthTransactions.filterIsInstance<Income>().sumOf { it.amount },
            monthExpense = monthTransactions.filterIsInstance<Expense>().sumOf { it.amount },
            monthLabel = FinanceRules.monthLabel(now),
            daysLeftInMonth = FinanceRules.daysLeftInMonth(now),
            isLoadingDemo = state.isLoadingDemo,
            message = state.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpendLogUiState()
    )

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransaction(id)
            when (val result = repository.deleteTransaction(id)) {
                is OperationResult.Success -> {
                    lastDeleted = transaction
                    showMessage(result.message, actionLabel = "Undo")
                }
                else -> show(result)
            }
        }
    }

    fun undoDelete() {
        val transaction = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { show(repository.restoreTransaction(transaction)) }
    }

    fun saveAccount(draft: AccountDraft) {
        viewModelScope.launch { show(repository.saveAccount(draft)) }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch { show(repository.deleteAccount(id)) }
    }

    fun saveBudget(category: String, monthlyLimit: Double, isNew: Boolean) {
        viewModelScope.launch { show(repository.saveBudget(category, monthlyLimit, isNew)) }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch { show(repository.deleteBudget(category)) }
    }

    fun loadDemoMonth() {
        if (transient.value.isLoadingDemo) return
        transient.update { it.copy(isLoadingDemo = true) }
        viewModelScope.launch {
            val result = repository.loadDemoMonth()
            transient.update { it.copy(isLoadingDemo = false) }
            show(result)
        }
    }

    fun showMessage(text: String, actionLabel: String? = null) {
        transient.update { it.copy(message = UserMessage(++nextMessageId, text, actionLabel)) }
    }

    fun consumeMessage(id: Long) {
        transient.update { if (it.message?.id == id) it.copy(message = null) else it }
    }

    private fun show(result: OperationResult) {
        showMessage(
            when (result) {
                is OperationResult.Success -> result.message
                is OperationResult.Error -> result.reason
                OperationResult.NotFound -> "This item no longer exists"
            }
        )
    }
}
