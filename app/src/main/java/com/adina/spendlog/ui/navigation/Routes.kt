package com.adina.spendlog.ui.navigation

object Routes {
    const val Transactions = "transactions"
    const val Overview = "overview"
    const val Accounts = "accounts"
    const val TransactionDetail = "transaction/{transactionId}"

    fun transactionDetail(transactionId: Int): String = "transaction/$transactionId"

    val bottomBarRoutes = setOf(Transactions, Overview, Accounts)
}
