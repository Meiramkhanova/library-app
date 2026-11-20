package com.adina.spendlog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.adina.spendlog.model.AccountBalance

@Composable
fun AccountPicker(
    balances: List<AccountBalance>,
    selectedAccountId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        balances.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item.account.id) }
            ) {
                RadioButton(
                    selected = selectedAccountId == item.account.id,
                    onClick = { onSelect(item.account.id) }
                )
                Column {
                    Text(text = item.account.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${item.account.type} · balance ${"%.0f".format(item.balance)} ₸",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
