package com.adina.spendlog.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.SpendLogUiState
import com.adina.spendlog.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    uiState: SpendLogUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = uiState.transactions.find { it.id == transactionId }
    val accountName = uiState.balances
        .find { it.account.id == transaction?.accountId }
        ?.account
        ?.name
        ?: "Unknown"

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Entry details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (transaction == null) {
            Text(
                text = "Entry #$transactionId was not found.",
                modifier = Modifier.padding(16.dp)
            )
            return
        }

        val isExpense = transaction is Expense
        val amountColor = if (isExpense) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.secondary
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = transaction.note, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${transaction.kind} · ${transaction.category}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Amount", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isExpense) "−" else "+"}${"%.2f".format(transaction.amount)} ₸",
                        style = MaterialTheme.typography.headlineSmall,
                        color = amountColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Account: $accountName")
                    Text(
                        text = "Date: " +
                            SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
                                .format(Date(transaction.timestampMillis))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.describe(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
