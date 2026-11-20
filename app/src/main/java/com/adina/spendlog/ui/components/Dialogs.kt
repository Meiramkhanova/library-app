package com.adina.spendlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.AccountBalance
import com.adina.spendlog.model.Categories

@Composable
fun AddTransactionDialog(
    balances: List<AccountBalance>,
    onDismiss: () -> Unit,
    onAdd: (kind: String, accountId: Int, category: String, amount: Double, note: String) -> Unit
) {
    var kind by remember { mutableStateOf("Expense") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Categories.expense.first()) }
    var selectedAccountId by remember { mutableStateOf(balances.firstOrNull()?.account?.id) }

    val categoryOptions = if (kind == "Income") Categories.income else Categories.expense

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log entry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Expense", "Income").forEach { option ->
                        FilterChip(
                            selected = kind == option,
                            onClick = {
                                kind = option
                                category = if (option == "Income") {
                                    Categories.income.first()
                                } else {
                                    Categories.expense.first()
                                }
                            },
                            label = { Text(option) }
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        amount = value.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Amount (₸)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 200) note = it },
                    label = { Text("What did you do?") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryOptions.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option) }
                        )
                    }
                }
                AccountPicker(
                    balances = balances,
                    selectedAccountId = selectedAccountId,
                    onSelect = { selectedAccountId = it }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                    note.isNotBlank() &&
                    selectedAccountId != null,
                onClick = {
                    val accountId = selectedAccountId ?: return@Button
                    onAdd(kind, accountId, category, amount.toDouble(), note.trim())
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, name: String, startingBalance: Double, creditLimit: Double) -> Unit
) {
    var type by remember { mutableStateOf("Cash") }
    var name by remember { mutableStateOf("") }
    var startingBalance by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("100000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cash", "Card", "Savings").forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option) }
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = startingBalance,
                    onValueChange = { value ->
                        startingBalance = value.filter { it.isDigit() || it == '.' || it == '-' }
                    },
                    label = { Text("Starting balance (₸)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (type == "Card") {
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { value ->
                            creditLimit = value.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text("Credit limit (₸)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && startingBalance.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(
                        type,
                        name.trim(),
                        startingBalance.toDoubleOrNull() ?: 0.0,
                        creditLimit.toDoubleOrNull() ?: 0.0
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
