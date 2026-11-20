package com.adina.spendlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.Account
import com.adina.spendlog.model.AccountDraft
import com.adina.spendlog.model.AccountType
import com.adina.spendlog.model.Validation
import com.adina.spendlog.model.formatAmountInput

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    showError: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    helper: String? = null
) {
    val visibleError = error?.takeIf { showError }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = visibleError != null,
        supportingText = (visibleError ?: helper)?.let { text -> { Text(text) } },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}

@Composable
fun AccountFormDialog(
    initial: Account?,
    takenNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (AccountDraft) -> Unit
) {
    var type by rememberSaveable { mutableStateOf(initial?.type ?: AccountType.CASH) }
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var startingBalance by rememberSaveable {
        mutableStateOf(initial?.let { formatAmountInput(it.startingBalance) } ?: "0")
    }
    var creditLimit by rememberSaveable {
        mutableStateOf(formatAmountInput(initial?.creditLimit?.takeIf { it > 0 } ?: 100000.0))
    }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val nameError = Validation.accountNameError(name, takenNames)
    val balanceError = Validation.startingBalanceError(startingBalance, type)
    val limitError = if (type == AccountType.CARD) Validation.creditLimitError(creditLimit) else null
    val isValid = nameError == null && balanceError == null && limitError == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add account" else "Edit account") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChoiceChipRow(options = AccountType.all, selected = type, onSelect = { type = it })
                ValidatedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = "Name",
                    error = nameError,
                    showError = submitted || name.isNotEmpty()
                )
                ValidatedTextField(
                    value = startingBalance,
                    onValueChange = { startingBalance = Validation.cleanNumberInput(it, allowMinus = true) },
                    label = "Starting balance (₸)",
                    error = balanceError,
                    showError = true,
                    keyboardType = KeyboardType.Decimal
                )
                if (type == AccountType.CARD) {
                    ValidatedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = Validation.cleanNumberInput(it) },
                        label = "Credit limit (₸)",
                        error = limitError,
                        showError = true,
                        keyboardType = KeyboardType.Decimal,
                        helper = "How far below 0 this card may go"
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                submitted = true
                if (isValid) {
                    onSave(
                        AccountDraft(
                            id = initial?.id,
                            type = type,
                            name = name.trim(),
                            startingBalance = startingBalance.toDouble(),
                            creditLimit = if (type == AccountType.CARD) creditLimit.toDouble() else 0.0
                        )
                    )
                }
            }) { Text(if (initial == null) "Add" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BudgetFormDialog(
    editingCategory: String?,
    initialLimit: Double?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(editingCategory ?: availableCategories.firstOrNull()) }
    var limit by rememberSaveable { mutableStateOf(initialLimit?.let { formatAmountInput(it) } ?: "") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val limitError = Validation.amountError(limit, emptyMessage = "Enter a monthly limit")
    val categoryError = if (category == null) "Choose a category" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingCategory == null) "New budget" else "Edit \"$editingCategory\" budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editingCategory == null) {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    ChoiceChipRow(
                        options = availableCategories,
                        selected = category,
                        onSelect = { category = it }
                    )
                    if (submitted && categoryError != null) {
                        Text(categoryError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
                ValidatedTextField(
                    value = limit,
                    onValueChange = { limit = Validation.cleanNumberInput(it) },
                    label = "Monthly limit (₸)",
                    error = limitError,
                    showError = submitted || limit.isNotEmpty(),
                    keyboardType = KeyboardType.Decimal
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                submitted = true
                val selected = category
                if (limitError == null && selected != null) onSave(selected, limit.toDouble())
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
