package com.adina.spendlog.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY id")
    suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Int): AccountEntity?

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)
}

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countForAccount(accountId: Int): Int

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY category")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAll(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getByCategory(category: String): BudgetEntity?

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)
}
