package com.adina.spendlog.data

import com.adina.spendlog.data.local.AccountEntity
import com.adina.spendlog.data.local.BudgetEntity
import com.adina.spendlog.data.local.TransactionEntity
import com.adina.spendlog.model.Account
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.AccountType
import com.adina.spendlog.model.Budget
import com.adina.spendlog.model.CardAccount
import com.adina.spendlog.model.CashAccount
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.Income
import com.adina.spendlog.model.SavingsAccount
import com.adina.spendlog.model.Transaction
import com.adina.spendlog.model.TransactionDraft
import com.adina.spendlog.model.TransactionKind

fun AccountEntity.toDomain(): Account = when (type) {
    AccountType.CARD -> CardAccount(id, name, startingBalance, creditLimit)
    AccountType.SAVINGS -> SavingsAccount(id, name, startingBalance)
    else -> CashAccount(id, name, startingBalance)
}

fun AccountDraft.toEntity(): AccountEntity = AccountEntity(
    id = id ?: 0,
    name = name.trim(),
    type = type,
    startingBalance = startingBalance,
    creditLimit = if (type == AccountType.CARD) creditLimit else 0.0
)

fun TransactionEntity.toDomain(): Transaction = when (kind) {
    TransactionKind.INCOME -> Income(id, accountId, category, amount, note, timestampMillis)
    else -> Expense(id, accountId, category, amount, note, timestampMillis)
}

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = accountId,
    kind = kind,
    category = category,
    amount = amount,
    note = note,
    timestampMillis = timestampMillis
)

fun TransactionDraft.toDomain(): Transaction {
    val newId = id ?: 0
    return when (kind) {
        TransactionKind.INCOME -> Income(newId, accountId, category, amount, note.trim(), timestampMillis)
        else -> Expense(newId, accountId, category, amount, note.trim(), timestampMillis)
    }
}

fun BudgetEntity.toDomain(): Budget = Budget(category, monthlyLimit)
