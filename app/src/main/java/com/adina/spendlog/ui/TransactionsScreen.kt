package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.formatMoney
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.FilterBar
import com.adina.spendlog.ui.components.HeroCard
import com.adina.spendlog.ui.components.TransactionCard
import com.adina.spendlog.ui.viewmodel.SpendLogUiState

@Composable
fun TransactionsScreen(
    uiState: SpendLogUiState,
    onAddTransaction: () -> Unit,
    onLoadDemo: () -> Unit,
    onOpenTransaction: (transactionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedKind by rememberSaveable { mutableStateOf("All") }

    if (uiState.isLoading) {
        LoadingBox(modifier)
        return
    }

    val visible = uiState.transactions.filter { tx ->
        (selectedKind == "All" || tx.kind == selectedKind) &&
            (tx.note.contains(query, ignoreCase = true) || tx.category.contains(query, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            ScreenHeader(title = "SpendLog", subtitle = "${uiState.transactions.size} entries")
        }
        item(key = "hero") {
            HeroCard {
                Text("Total balance", style = MaterialTheme.typography.titleMedium)
                Text(formatMoney(uiState.totalBalance), style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onPrimary))
                Spacer(modifier = Modifier.height(6.dp))
                Text("This month: +${formatMoney(uiState.monthIncome)} · −${formatMoney(uiState.monthExpense)}")
            }
        }
        item(key = "actions") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAddTransaction) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log entry")
                }
                DemoButton(isLoading = uiState.isLoadingDemo, onClick = onLoadDemo)
            }
        }

        if (uiState.transactions.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "No entries yet",
                    message = "Log what you spent or earned today, or load a demo month to try the app."
                )
            }
        } else {
            item(key = "filters") {
                FilterBar(
                    query = query,
                    onQueryChange = { query = it },
                    selectedKind = selectedKind,
                    onKindChange = { selectedKind = it }
                )
            }
            if (visible.isEmpty()) {
                item(key = "no_results") {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "Nothing matches",
                        message = "Try another search or switch the filter back to All."
                    )
                }
            } else {
                items(visible, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        accountName = uiState.accountName(tx.accountId),
                        onOpen = { onOpenTransaction(tx.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoButton(isLoading: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = !isLoading) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.Download, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isLoading) "Loading…" else "Demo month")
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
