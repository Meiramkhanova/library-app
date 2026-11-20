package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adina.spendlog.model.FinanceRules
import com.adina.spendlog.model.TransactionKind
import com.adina.spendlog.ui.components.AccountPicker
import com.adina.spendlog.ui.components.ChoiceChipRow
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.ValidatedTextField
import com.adina.spendlog.ui.viewmodel.TransactionFormUiState
import com.adina.spendlog.ui.viewmodel.TransactionFormViewModel

@Composable
fun TransactionFormScreen(
    viewModel: TransactionFormViewModel,
    onBack: () -> Unit,
    onSaved: (message: String) -> Unit,
    onOpenAccounts: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let(onSaved)
    }

    TransactionFormContent(
        state = state,
        onBack = onBack,
        onKindChange = viewModel::onKindChange,
        onAmountChange = viewModel::onAmountChange,
        onNoteChange = viewModel::onNoteChange,
        onCategoryChange = viewModel::onCategoryChange,
        onAccountChange = viewModel::onAccountChange,
        onSave = viewModel::save,
        onOpenAccounts = onOpenAccounts
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormContent(
    state: TransactionFormUiState,
    onBack: () -> Unit,
    onKindChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAccountChange: (Int) -> Unit,
    onSave: () -> Unit,
    onOpenAccounts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (state.isEditing) "Edit entry" else "New entry") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when {
            state.isLoading -> LoadingBox()
            state.notFound -> EmptyState(
                icon = Icons.Filled.SearchOff,
                title = "Entry not found",
                message = "It may have been deleted."
            ) {
                OutlinedButton(onClick = onBack) { Text("Go back") }
            }
            state.accounts.isEmpty() -> EmptyState(
                icon = Icons.Filled.AccountBalanceWallet,
                title = "No accounts yet",
                message = "Add Cash, Card or Savings first, then log your money."
            ) {
                Button(onClick = onOpenAccounts) { Text("Go to Accounts") }
            }
            else -> FormFields(
                state = state,
                onKindChange = onKindChange,
                onAmountChange = onAmountChange,
                onNoteChange = onNoteChange,
                onCategoryChange = onCategoryChange,
                onAccountChange = onAccountChange,
                onSave = onSave
            )
        }
    }
}

@Composable
private fun FormFields(
    state: TransactionFormUiState,
    onKindChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAccountChange: (Int) -> Unit,
    onSave: () -> Unit
) {
    val fields = state.fields
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel("Type")
        ChoiceChipRow(options = TransactionKind.all, selected = fields.kind, onSelect = onKindChange)

        ValidatedTextField(
            value = fields.amount,
            onValueChange = onAmountChange,
            label = "Amount (₸)",
            error = state.amountError,
            showError = state.showErrors || fields.amount.isNotEmpty(),
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.fillMaxWidth()
        )

        ValidatedTextField(
            value = fields.note,
            onValueChange = onNoteChange,
            label = "What did you do?",
            error = state.noteError,
            showError = state.showErrors,
            singleLine = false,
            helper = "${fields.note.length}/${FinanceRules.MAX_NOTE_LENGTH}",
            modifier = Modifier.fillMaxWidth()
        )

        SectionLabel("Category")
        ChoiceChipRow(options = state.categories, selected = fields.category, onSelect = onCategoryChange)

        SectionLabel("Account")
        AccountPicker(
            balances = state.accounts,
            selectedAccountId = fields.accountId,
            onSelect = onAccountChange
        )
        if (state.showErrors && state.accountError != null) {
            Text(state.accountError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        state.saveError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(error, modifier = Modifier.padding(12.dp))
            }
        }

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(if (state.isEditing) "Save changes" else "Save entry")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
