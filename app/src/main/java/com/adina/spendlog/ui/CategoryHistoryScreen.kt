package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.formatMoney
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.HeroCard
import com.adina.spendlog.ui.components.TransactionCard
import com.adina.spendlog.ui.viewmodel.SpendLogUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHistoryScreen(
    category: String,
    uiState: SpendLogUiState,
    onBack: () -> Unit,
    onOpenTransaction: (transactionId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = uiState.monthTransactions.filter { it is Expense && it.category == category }
    val spend = uiState.spendFor(category)

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(category) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "summary") {
                HeroCard {
                    Text("${uiState.monthLabel}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Spent ${formatMoney(spend?.spent ?: 0.0)}",
                        style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onPrimary)
                    )
                    val limit = spend?.limit
                    if (limit != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { spend.progress.coerceIn(0f, 1f) },
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (spend.isOverBudget) {
                                "Over the ${formatMoney(limit)} limit by ${formatMoney(-spend.remaining)}"
                            } else {
                                "${formatMoney(spend.remaining)} left of ${formatMoney(limit)}"
                            }
                        )
                    } else {
                        Text("No budget for this category")
                    }
                    Text("${entries.size} entries this month")
                }
            }

            if (entries.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "Nothing spent on $category",
                        message = "Expenses in this category for ${uiState.monthLabel} will show up here."
                    )
                }
            } else {
                items(entries, key = { it.id }) { tx ->
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
