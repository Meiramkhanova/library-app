package com.adina.spendlog.ui.navigation

import android.net.Uri

object Routes {
    const val Transactions = "transactions"
    const val Overview = "overview"
    const val Accounts = "accounts"

    const val TransactionIdArg = "transactionId"
    const val CategoryArg = "category"

    const val TransactionDetail = "transaction/{$TransactionIdArg}"
    const val TransactionForm = "transaction_form?$TransactionIdArg={$TransactionIdArg}"
    const val CategoryHistory = "category/{$CategoryArg}"

    fun transactionDetail(transactionId: Int): String = "transaction/$transactionId"
    fun newTransaction(): String = "transaction_form"
    fun editTransaction(transactionId: Int): String = "transaction_form?$TransactionIdArg=$transactionId"
    fun categoryHistory(category: String): String = "category/${Uri.encode(category)}"

    val bottomBarRoutes = setOf(Transactions, Overview, Accounts)
}
