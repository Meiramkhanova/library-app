package com.adina.spendlog.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.adina.spendlog.SpendLogApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SpendLogViewModel(spendLogApplication().container.financeRepository)
        }
        initializer {
            TransactionFormViewModel(
                savedStateHandle = createSavedStateHandle(),
                repository = spendLogApplication().container.financeRepository
            )
        }
    }
}

private fun CreationExtras.spendLogApplication(): SpendLogApplication =
    this[APPLICATION_KEY] as SpendLogApplication
