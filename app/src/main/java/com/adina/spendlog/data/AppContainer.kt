package com.adina.spendlog.data

import android.content.Context
import com.adina.spendlog.data.local.SpendLogDatabase
import com.adina.spendlog.data.repository.FinanceRepository
import com.adina.spendlog.data.repository.OfflineFinanceRepository

class AppContainer(context: Context) {

    private val database = SpendLogDatabase.getInstance(context)

    val financeRepository: FinanceRepository = OfflineFinanceRepository(
        accountDao = database.accountDao(),
        transactionDao = database.transactionDao(),
        budgetDao = database.budgetDao()
    )
}
