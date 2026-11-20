package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.FinanceRules
import com.adina.spendlog.model.formatMoney
import com.adina.spendlog.ui.components.AccountCard
import com.adina.spendlog.ui.components.AccountFormDialog
import com.adina.spendlog.ui.components.ConfirmDeleteDialog
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.HeroCard
import com.adina.spendlog.ui.viewmodel.SpendLogUiState

@Composable
fun AccountsScreen(
    uiState: SpendLogUiState,
    onSaveAccount: (AccountDraft) -> Unit,
    onDeleteAccount: (accountId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<Int?>(null) }

    if (uiState.isLoading) {
        LoadingBox(modifier)
        return
    }

    val counts = uiState.transactions.groupingBy { it.accountId }.eachCount()
    val canAdd = uiState.balances.size < FinanceRules.MAX_ACCOUNTS

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            ScreenHeader(title = "Accounts", subtitle = "${uiState.balances.size} of ${FinanceRules.MAX_ACCOUNTS} wallets")
        }
        item(key = "hero") {
            HeroCard {
                Text("Total balance", style = MaterialTheme.typography.titleMedium)
                Text(
                    formatMoney(uiState.totalBalance),
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
        item(key = "add") {
            Button(
                onClick = {
                    editingId = null
                    showDialog = true
                },
                enabled = canAdd
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (canAdd) "Add account" else "Account limit reached")
            }
        }

        if (uiState.balances.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = "No accounts yet",
                    message = "Add Cash, a Card or Savings to start logging money."
                )
            }
        } else {
            items(uiState.balances, key = { it.account.id }) { balance ->
                AccountCard(
                    balance = balance,
                    transactionCount = counts[balance.account.id] ?: 0,
                    onEdit = {
                        editingId = balance.account.id
                        showDialog = true
                    },
                    onDelete = { pendingDeleteId = balance.account.id }
                )
            }
        }
    }

    if (showDialog) {
        val editing = editingId?.let { id -> uiState.balances.find { it.account.id == id }?.account }
        AccountFormDialog(
            initial = editing,
            takenNames = uiState.balances.map { it.account }.filter { it.id != editing?.id }.map { it.name },
            onDismiss = { showDialog = false },
            onSave = { draft ->
                onSaveAccount(draft)
                showDialog = false
            }
        )
    }

    pendingDeleteId?.let { id ->
        val account = uiState.balances.find { it.account.id == id }?.account
        val count = counts[id] ?: 0
        if (account == null) {
            pendingDeleteId = null
        } else {
            ConfirmDeleteDialog(
                title = "Delete \"${account.name}\"?",
                message = if (count > 0) {
                    "This account has $count entries, so it can't be deleted until you remove or move them."
                } else {
                    "This account has no entries and will be removed."
                },
                onConfirm = {
                    onDeleteAccount(id)
                    pendingDeleteId = null
                },
                onDismiss = { pendingDeleteId = null }
            )
        }
    }
}
