package com.adina.spendlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val KindFilters = listOf("All", "Expense", "Income")

@Composable
fun FilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedKind: String,
    onKindChange: (String) -> Unit,
    onlyExpenses: Boolean,
    onOnlyExpensesChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search by note") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KindFilters.forEach { kind ->
                FilterChip(
                    selected = selectedKind == kind,
                    onClick = { onKindChange(kind) },
                    label = { Text(kind) }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = onlyExpenses, onCheckedChange = onOnlyExpensesChange)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Only expenses")
        }
    }
}
