package com.adina.spendlog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.AccountBalance
import com.adina.spendlog.model.CardAccount
import com.adina.spendlog.model.formatMoney

@Composable
fun AccountCard(
    balance: AccountBalance,
    transactionCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account = balance.account
    SurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${account.type} · $transactionCount entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit ${account.name}")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Delete ${account.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatMoney(balance.balance),
            style = MaterialTheme.typography.headlineSmall,
            color = if (balance.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        if (account is CardAccount) {
            Text(
                text = "Can spend up to ${formatMoney(account.maxSpendable(balance.balance))} " +
                    "(credit limit ${formatMoney(account.creditLimit)})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
