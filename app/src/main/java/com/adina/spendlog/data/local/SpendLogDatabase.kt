package com.adina.spendlog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AccountEntity::class, TransactionEntity::class, BudgetEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SpendLogDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var instance: SpendLogDatabase? = null

        fun getInstance(context: Context): SpendLogDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpendLogDatabase::class.java,
                    "spendlog.db"
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { instance = it }
            }
    }
}

private object SeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        val accountSql = "INSERT INTO accounts (id, name, type, startingBalance, creditLimit) VALUES (?, ?, ?, ?, ?)"
        db.execSQL(accountSql, arrayOf<Any>(1, "Cash", "Cash", 8000.0, 0.0))
        db.execSQL(accountSql, arrayOf<Any>(2, "Kaspi", "Card", 52000.0, 100000.0))
        db.execSQL(accountSql, arrayOf<Any>(3, "Savings", "Savings", 150000.0, 0.0))

        val budgetSql = "INSERT INTO budgets (category, monthlyLimit) VALUES (?, ?)"
        db.execSQL(budgetSql, arrayOf<Any>("Food", 25000.0))
        db.execSQL(budgetSql, arrayOf<Any>("Transport", 8000.0))
        db.execSQL(budgetSql, arrayOf<Any>("Study", 15000.0))
        db.execSQL(budgetSql, arrayOf<Any>("Fun", 20000.0))

        val txSql = "INSERT INTO transactions (accountId, kind, category, amount, note, timestampMillis) " +
            "VALUES (?, ?, ?, ?, ?, ?)"
        db.execSQL(txSql, arrayOf<Any>(2, "Income", "Salary", 50000.0, "подработка за неделю", now - 4 * day))
        db.execSQL(txSql, arrayOf<Any>(2, "Expense", "Food", 2200.0, "ланч в столовой", now - day))
        db.execSQL(txSql, arrayOf<Any>(1, "Expense", "Transport", 600.0, "такси домой после лабы", now - 2 * day))
        db.execSQL(txSql, arrayOf<Any>(2, "Expense", "Fun", 4500.0, "подарок подруге", now - 5 * day))
    }
}
