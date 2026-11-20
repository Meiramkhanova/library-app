package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.SpendLogUiState
import com.adina.spendlog.model.Finance
import com.adina.spendlog.ui.components.AccountCard
import com.adina.spendlog.ui.components.AddAccountDialog

@Composable
fun AccountsScreen(
    uiState: SpendLogUiState,
    onAddAccount: (type: String, name: String, startingBalance: Double, creditLimit: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val counts = uiState.transactions.groupingBy { it.accountId }.eachCount()

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Accounts", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${uiState.balances.size} wallets · max ${Finance.MAX_ACCOUNTS} " +
                "(Cash/Savings cannot go negative)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(onClick = { showAddDialog = true }) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add account")
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Total balance", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${"%.0f".format(uiState.balances.sumOf { it.balance })} ₸",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.balances.isEmpty()) {
            Text(
                text = "No accounts yet. Add Cash, Card, or Savings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.balances, key = { it.account.id }) { balance ->
                    AccountCard(
                        balance = balance,
                        transactionCount = counts[balance.account.id] ?: 0
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, name, starting, credit ->
                onAddAccount(type, name, starting, credit)
                showAddDialog = false
            }
        )
    }
}
