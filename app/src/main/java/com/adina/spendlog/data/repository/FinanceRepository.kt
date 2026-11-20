package com.adina.spendlog.data.repository

import com.adina.spendlog.model.Account
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.Budget
import com.adina.spendlog.model.OperationResult
import com.adina.spendlog.model.Transaction
import com.adina.spendlog.model.TransactionDraft
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {

    val accounts: Flow<List<Account>>
    val transactions: Flow<List<Transaction>>
    val budgets: Flow<List<Budget>>

    suspend fun getTransaction(id: Int): Transaction?
    suspend fun saveTransaction(draft: TransactionDraft): OperationResult
    suspend fun deleteTransaction(id: Int): OperationResult
    suspend fun restoreTransaction(transaction: Transaction): OperationResult

    suspend fun saveAccount(draft: AccountDraft): OperationResult
    suspend fun deleteAccount(id: Int): OperationResult

    suspend fun saveBudget(category: String, monthlyLimit: Double, isNew: Boolean): OperationResult
    suspend fun deleteBudget(category: String): OperationResult

    suspend fun loadDemoMonth(): OperationResult
}
