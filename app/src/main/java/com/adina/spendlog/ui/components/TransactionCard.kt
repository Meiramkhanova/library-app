package com.adina.spendlog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.Expense
import com.adina.spendlog.model.Income
import com.adina.spendlog.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
    transaction: Transaction,
    accountName: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction is Expense
    val amountPrefix = if (isExpense) "−" else "+"
    val amountColor = if (isExpense) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = iconFor(transaction),
                        contentDescription = transaction.kind,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = transaction.note, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${transaction.category} · $accountName · ${formatDate(transaction.timestampMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$amountPrefix${"%.0f".format(transaction.amount)} ₸",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = transaction.describe(), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleExpanded) {
                    Text(if (expanded) "Hide" else "Preview")
                }
                TextButton(onClick = onOpen) {
                    Text("Open")
                }
            }
        }
    }
}

private fun iconFor(transaction: Transaction): ImageVector = when (transaction) {
    is Expense -> Icons.Filled.ArrowDownward
    is Income -> Icons.Filled.ArrowUpward
    else -> Icons.Filled.ArrowDownward
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))
