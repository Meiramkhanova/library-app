package com.adina.spendlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adina.spendlog.model.AccountBalance
import com.adina.spendlog.model.Budget
import com.adina.spendlog.model.CardAccount
import com.adina.spendlog.model.CashAccount
import com.adina.spendlog.model.CategorySpend
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.Finance
import com.adina.spendlog.model.Income
import com.adina.spendlog.model.OperationResult
import com.adina.spendlog.model.SavingsAccount
import com.adina.spendlog.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpendLogUiState(
    val transactions: List<Transaction> = emptyList(),
    val balances: List<AccountBalance> = emptyList(),
    val categorySpend: List<CategorySpend> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthLabel: String = "",
    val isLoadingDemo: Boolean = false,
    val message: String? = null
)

class SpendLogViewModel : ViewModel() {

    private val finance = Finance("Adina")

    private val _uiState = MutableStateFlow(SpendLogUiState())
    val uiState: StateFlow<SpendLogUiState> = _uiState.asStateFlow()

    init {
        seed()
        refresh()
    }

    private fun seed() {
        finance.addAccount(CashAccount(1, "Cash", 8000.0))
        finance.addAccount(CardAccount(2, "Kaspi", 52000.0, creditLimit = 100000.0))
        finance.addAccount(SavingsAccount(3, "Savings", 150000.0))

        finance.addBudget(Budget("Food", 25000.0))
        finance.addBudget(Budget("Transport", 8000.0))
        finance.addBudget(Budget("Study", 15000.0))
        finance.addBudget(Budget("Fun", 20000.0))

        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        finance.addTransaction("Expense", 2, "Food", 2200.0, "ланч в столовой", now - day)
        finance.addTransaction("Expense", 1, "Transport", 600.0, "такси домой после лабы", now - 2 * day)
        finance.addTransaction("Income", 2, "Salary", 50000.0, "подработка за неделю", now - 4 * day)
        finance.addTransaction("Expense", 2, "Fun", 4500.0, "подарок подруге", now - 5 * day)
    }

    fun addTransaction(
        kind: String,
        accountId: Int,
        category: String,
        amount: Double,
        note: String
    ) {
        refresh(message = finance.addTransaction(kind, accountId, category, amount, note).toMessage())
    }

    fun addAccount(
        type: String,
        name: String,
        startingBalance: Double,
        creditLimit: Double
    ) {
        if (finance.allAccounts().size >= Finance.MAX_ACCOUNTS) {
            refresh(message = "You already have ${Finance.MAX_ACCOUNTS} accounts")
            return
        }
        refresh(message = finance.addAccount(type, name, startingBalance, creditLimit).toMessage())
    }

    fun transactionById(id: Int): Transaction? = finance.findTransaction(id)

    fun loadDemoMonth() {
        if (_uiState.value.isLoadingDemo) return
        refresh(loading = true)
        viewModelScope.launch {
            val demo = finance.fetchDemoMonth()
            demo.forEach { tx ->
                when (tx) {
                    is Expense -> finance.addTransaction(
                        "Expense", tx.accountId, tx.category, tx.amount, tx.note, tx.timestampMillis
                    )
                    is Income -> finance.addTransaction(
                        "Income", tx.accountId, tx.category, tx.amount, tx.note, tx.timestampMillis
                    )
                }
            }
            refresh(message = "Added ${demo.size} demo entries", loading = false)
        }
    }

    fun consumeMessage() {
        refresh(message = null)
    }

    private fun refresh(
        message: String? = _uiState.value.message,
        loading: Boolean = _uiState.value.isLoadingDemo
    ) {
        _uiState.value = SpendLogUiState(
            transactions = finance.allTransactions(),
            balances = finance.balances(),
            categorySpend = finance.spentByCategory(),
            budgets = finance.allBudgets(),
            totalIncome = finance.totalIncome(),
            totalExpense = finance.totalExpense(),
            monthLabel = finance.monthLabel(),
            isLoadingDemo = loading,
            message = message
        )
    }

    private fun OperationResult.toMessage(): String = when (this) {
        is OperationResult.Success -> message
        is OperationResult.Error -> reason
        OperationResult.NotFound -> "Not found"
    }
}
