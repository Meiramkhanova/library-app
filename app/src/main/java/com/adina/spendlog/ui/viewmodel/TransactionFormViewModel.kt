package com.adina.spendlog.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adina.spendlog.data.repository.FinanceRepository
import com.adina.spendlog.model.AccountBalance
import com.adina.spendlog.model.Categories
import com.adina.spendlog.model.FinanceRules
import com.adina.spendlog.model.OperationResult
import com.adina.spendlog.model.TransactionDraft
import com.adina.spendlog.model.TransactionKind
import com.adina.spendlog.model.Validation
import com.adina.spendlog.model.formatAmountInput
import com.adina.spendlog.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionFormFields(
    val kind: String = TransactionKind.EXPENSE,
    val amount: String = "",
    val note: String = "",
    val category: String = Categories.expense.first(),
    val accountId: Int? = null,
    val timestampMillis: Long? = null
)

private data class FormStatus(
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedMessage: String? = null
)

data class TransactionFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val fields: TransactionFormFields = TransactionFormFields(),
    val accounts: List<AccountBalance> = emptyList(),
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedMessage: String? = null
) {
    val categories: List<String> get() = Categories.forKind(fields.kind)
    val amountError: String? get() = Validation.amountError(fields.amount)
    val noteError: String? get() = Validation.noteError(fields.note)
    val accountError: String?
        get() = if (accounts.none { it.account.id == fields.accountId }) "Choose an account" else null
    val isValid: Boolean get() = amountError == null && noteError == null && accountError == null
}

class TransactionFormViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: FinanceRepository
) : ViewModel() {

    private val transactionId: Int? =
        savedStateHandle.get<Int>(Routes.TransactionIdArg)?.takeIf { it > 0 }

    private val needsInitialLoad = transactionId != null && savedStateHandle.get<Boolean>(KEY_LOADED) != true

    private val fields = MutableStateFlow(restoreFields())
    private val status = MutableStateFlow(FormStatus(isLoading = needsInitialLoad))

    val uiState: StateFlow<TransactionFormUiState> = combine(
        fields,
        status,
        repository.accounts,
        repository.transactions
    ) { fields, status, accounts, transactions ->
        val balances = FinanceRules.balances(accounts, transactions)
        val accountId = fields.accountId?.takeIf { id -> balances.any { it.account.id == id } }
            ?: balances.firstOrNull()?.account?.id
        TransactionFormUiState(
            isEditing = transactionId != null,
            isLoading = status.isLoading,
            notFound = status.notFound,
            fields = fields.copy(accountId = accountId),
            accounts = balances,
            showErrors = status.showErrors,
            isSaving = status.isSaving,
            saveError = status.saveError,
            savedMessage = status.savedMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionFormUiState(isEditing = transactionId != null)
    )

    init {
        if (needsInitialLoad && transactionId != null) {
            viewModelScope.launch {
                val transaction = repository.getTransaction(transactionId)
                if (transaction == null) {
                    status.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    updateFields {
                        TransactionFormFields(
                            kind = transaction.kind,
                            amount = formatAmountInput(transaction.amount),
                            note = transaction.note,
                            category = transaction.category,
                            accountId = transaction.accountId,
                            timestampMillis = transaction.timestampMillis
                        )
                    }
                    savedStateHandle[KEY_LOADED] = true
                    status.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onKindChange(kind: String) = updateFields {
        val options = Categories.forKind(kind)
        it.copy(kind = kind, category = if (it.category in options) it.category else options.first())
    }

    fun onAmountChange(value: String) = updateFields { it.copy(amount = Validation.cleanNumberInput(value)) }

    fun onNoteChange(value: String) {
        if (value.length <= FinanceRules.MAX_NOTE_LENGTH) updateFields { it.copy(note = value) }
    }

    fun onCategoryChange(category: String) = updateFields { it.copy(category = category) }

    fun onAccountChange(accountId: Int) = updateFields { it.copy(accountId = accountId) }

    fun save() {
        val state = uiState.value
        if (state.isSaving) return
        status.update { it.copy(showErrors = true, saveError = null) }
        if (!state.isValid) return

        val form = state.fields
        val accountId = form.accountId ?: return
        status.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = repository.saveTransaction(
                TransactionDraft(
                    id = transactionId,
                    kind = form.kind,
                    accountId = accountId,
                    category = form.category,
                    amount = form.amount.trim().toDouble(),
                    note = form.note.trim(),
                    timestampMillis = form.timestampMillis ?: System.currentTimeMillis()
                )
            )
            status.update {
                when (result) {
                    is OperationResult.Success -> it.copy(isSaving = false, savedMessage = result.message)
                    is OperationResult.Error -> it.copy(isSaving = false, saveError = result.reason)
                    OperationResult.NotFound -> it.copy(isSaving = false, notFound = true)
                }
            }
        }
    }

    private fun updateFields(transform: (TransactionFormFields) -> TransactionFormFields) {
        fields.update(transform)
        val value = fields.value
        savedStateHandle[KEY_KIND] = value.kind
        savedStateHandle[KEY_AMOUNT] = value.amount
        savedStateHandle[KEY_NOTE] = value.note
        savedStateHandle[KEY_CATEGORY] = value.category
        savedStateHandle[KEY_ACCOUNT] = value.accountId ?: NO_ID
        savedStateHandle[KEY_TIMESTAMP] = value.timestampMillis ?: NO_TIMESTAMP
    }

    private fun restoreFields(): TransactionFormFields {
        val defaults = TransactionFormFields()
        return TransactionFormFields(
            kind = savedStateHandle[KEY_KIND] ?: defaults.kind,
            amount = savedStateHandle[KEY_AMOUNT] ?: defaults.amount,
            note = savedStateHandle[KEY_NOTE] ?: defaults.note,
            category = savedStateHandle[KEY_CATEGORY] ?: defaults.category,
            accountId = savedStateHandle.get<Int>(KEY_ACCOUNT)?.takeIf { it != NO_ID },
            timestampMillis = savedStateHandle.get<Long>(KEY_TIMESTAMP)?.takeIf { it != NO_TIMESTAMP }
        )
    }

    private companion object {
        const val KEY_LOADED = "form_loaded"
        const val KEY_KIND = "form_kind"
        const val KEY_AMOUNT = "form_amount"
        const val KEY_NOTE = "form_note"
        const val KEY_CATEGORY = "form_category"
        const val KEY_ACCOUNT = "form_account"
        const val KEY_TIMESTAMP = "form_timestamp"
        const val NO_ID = -1
        const val NO_TIMESTAMP = -1L
    }
}
