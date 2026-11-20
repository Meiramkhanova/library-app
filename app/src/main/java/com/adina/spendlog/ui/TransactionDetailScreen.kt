package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.formatMoney
import com.adina.spendlog.model.formatMoneyPrecise
import com.adina.spendlog.ui.components.ConfirmDeleteDialog
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.SurfaceCard
import com.adina.spendlog.ui.components.budgetStatusColor
import com.adina.spendlog.ui.components.formatLongDate
import com.adina.spendlog.ui.viewmodel.SpendLogUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    uiState: SpendLogUiState,
    onBack: () -> Unit,
    onEdit: (transactionId: Int) -> Unit,
    onDelete: (transactionId: Int) -> Unit,
    onOpenCategory: (category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = uiState.transaction(transactionId)
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Entry details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (transaction != null) {
                    IconButton(onClick = { onEdit(transactionId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        when {
            uiState.isLoading -> LoadingBox()
            transaction == null -> EmptyState(
                icon = Icons.Filled.SearchOff,
                title = "Entry not found",
                message = "It may have been deleted."
            ) {
                OutlinedButton(onClick = onBack) { Text("Back to entries") }
            }
            else -> {
                val isExpense = transaction is Expense
                val spend = if (isExpense) uiState.spendFor(transaction.category) else null

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = (if (isExpense) "−" else "+") + formatMoneyPrecise(transaction.amount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        )
                    )
                    Text(text = transaction.note, style = MaterialTheme.typography.titleLarge)

                    SurfaceCard {
                        DetailRow("Type", transaction.kind)
                        DetailRow("Category", transaction.category)
                        DetailRow("Account", uiState.accountName(transaction.accountId))
                        DetailRow("Date", formatLongDate(transaction.timestampMillis))
                    }

                    if (spend?.limit != null) {
                        SurfaceCard {
                            Text("${spend.category} budget this month", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${formatMoney(spend.spent)} of ${formatMoney(spend.limit)}")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { spend.progress.coerceIn(0f, 1f) },
                                color = budgetStatusColor(spend),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (isExpense) {
                        OutlinedButton(
                            onClick = { onOpenCategory(transaction.category) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("See all ${transaction.category} this month") }
                    }
                    Button(onClick = { onEdit(transactionId) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit entry")
                    }
                }
            }
        }
    }

    if (confirmDelete && transaction != null) {
        ConfirmDeleteDialog(
            title = "Delete this entry?",
            message = "\"${transaction.note}\" (${formatMoney(transaction.amount)}) will be removed. " +
                "You can undo right after.",
            onConfirm = {
                confirmDelete = false
                onDelete(transactionId)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
