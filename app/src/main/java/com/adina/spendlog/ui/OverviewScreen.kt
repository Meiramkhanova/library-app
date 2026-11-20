package com.adina.spendlog.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.formatMoney
import com.adina.spendlog.ui.components.BudgetCard
import com.adina.spendlog.ui.components.BudgetFormDialog
import com.adina.spendlog.ui.components.ConfirmDeleteDialog
import com.adina.spendlog.ui.components.EmptyState
import com.adina.spendlog.ui.components.HeroCard
import com.adina.spendlog.ui.components.MonthHistoryCard
import com.adina.spendlog.ui.viewmodel.SpendLogUiState

@Composable
fun OverviewScreen(
    uiState: SpendLogUiState,
    onSaveBudget: (category: String, limit: Double, isNew: Boolean) -> Unit,
    onDeleteBudget: (category: String) -> Unit,
    onOpenCategory: (category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var editingCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var presetCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }

    if (uiState.isLoading) {
        LoadingBox(modifier)
        return
    }

    fun openNewBudget(category: String? = null) {
        editingCategory = null
        presetCategory = category
        showBudgetDialog = true
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            ScreenHeader(
                title = "Overview",
                subtitle = "${uiState.monthLabel} · ${uiState.daysLeftInMonth} days left"
            )
        }
        item(key = "hero") {
            HeroCard {
                Text("This month", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Income: ${formatMoney(uiState.monthIncome)}")
                Text("Expenses: ${formatMoney(uiState.monthExpense)}")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Net: ${formatMoney(uiState.monthIncome - uiState.monthExpense)}",
                    style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onPrimary)
                )
                if (uiState.overBudgetCount > 0) {
                    Text("${uiState.overBudgetCount} budget(s) over the limit")
                }
            }
        }
        item(key = "history") {
            MonthHistoryCard(history = uiState.history)
        }
        item(key = "budgets_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Budgets", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { openNewBudget() },
                    enabled = uiState.categoriesWithoutBudget.isNotEmpty()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add budget")
                }
            }
        }

        if (uiState.categorySpend.isEmpty()) {
            item(key = "budgets_empty") {
                EmptyState(
                    icon = Icons.Filled.PieChart,
                    title = "No budgets yet",
                    message = "Set a monthly limit for a category and track how much is left."
                ) {
                    Button(onClick = { openNewBudget() }) { Text("Create first budget") }
                }
            }
        } else {
            items(uiState.categorySpend, key = { it.category }) { spend ->
                BudgetCard(
                    spend = spend,
                    daysLeft = uiState.daysLeftInMonth,
                    onOpen = { onOpenCategory(spend.category) },
                    onEdit = {
                        editingCategory = spend.category
                        presetCategory = null
                        showBudgetDialog = true
                    },
                    onDelete = { pendingDelete = spend.category },
                    onSetBudget = { openNewBudget(spend.category) }
                )
            }
        }
    }

    if (showBudgetDialog) {
        val editing = editingCategory
        val available = if (editing != null) {
            listOf(editing)
        } else {
            val free = uiState.categoriesWithoutBudget
            presetCategory?.let { preset -> listOf(preset) + free.filterNot { it == preset } } ?: free
        }
        BudgetFormDialog(
            editingCategory = editing,
            initialLimit = editing?.let { category -> uiState.budgets.find { it.category == category }?.monthlyLimit },
            availableCategories = available,
            onDismiss = { showBudgetDialog = false },
            onSave = { category, limit ->
                onSaveBudget(category, limit, editing == null)
                showBudgetDialog = false
            }
        )
    }

    pendingDelete?.let { category ->
        ConfirmDeleteDialog(
            title = "Delete \"$category\" budget?",
            message = "Your entries stay. Only the monthly limit for $category is removed.",
            onConfirm = {
                onDeleteBudget(category)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}
