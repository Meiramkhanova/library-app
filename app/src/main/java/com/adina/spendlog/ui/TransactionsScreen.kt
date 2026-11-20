package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.SpendLogUiState
import com.adina.spendlog.ui.components.AddTransactionDialog
import com.adina.spendlog.ui.components.FilterBar
import com.adina.spendlog.ui.components.TransactionCard

@Composable
fun TransactionsScreen(
    uiState: SpendLogUiState,
    onAddTransaction: (kind: String, accountId: Int, category: String, amount: Double, note: String) -> Unit,
    onLoadDemo: () -> Unit,
    onOpenTransaction: (transactionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedKind by rememberSaveable { mutableStateOf("All") }
    var onlyExpenses by rememberSaveable { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val accountNames = uiState.balances.associate { it.account.id to it.account.name }

    val visible = uiState.transactions.filter { tx ->
        (selectedKind == "All" || tx.kind == selectedKind) &&
            (!onlyExpenses || tx.kind == "Expense") &&
            (tx.note.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true))
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "SpendLog", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${uiState.transactions.size} entries · net " +
                "${"%.0f".format(uiState.totalIncome - uiState.totalExpense)} ₸",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        FilterBar(
            query = query,
            onQueryChange = { query = it },
            selectedKind = selectedKind,
            onKindChange = { selectedKind = it },
            onlyExpenses = onlyExpenses,
            onOnlyExpensesChange = { onlyExpenses = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log entry")
            }
            OutlinedButton(
                onClick = onLoadDemo,
                enabled = !uiState.isLoadingDemo
            ) {
                if (uiState.isLoadingDemo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Download, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Demo month")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (visible.isEmpty()) {
            Text(
                text = "Nothing found. Log what you spent or earned today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visible, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        accountName = accountNames[tx.accountId] ?: "Unknown",
                        expanded = expandedId == tx.id,
                        onToggleExpanded = {
                            expandedId = if (expandedId == tx.id) null else tx.id
                        },
                        onOpen = { onOpenTransaction(tx.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            balances = uiState.balances,
            onDismiss = { showAddDialog = false },
            onAdd = { kind, accountId, category, amount, note ->
                onAddTransaction(kind, accountId, category, amount, note)
                showAddDialog = false
            }
        )
    }
}
