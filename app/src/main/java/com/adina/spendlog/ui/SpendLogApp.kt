package com.adina.spendlog.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adina.spendlog.ui.navigation.Routes
import com.adina.spendlog.ui.viewmodel.AppViewModelProvider
import com.adina.spendlog.ui.viewmodel.SpendLogViewModel
import com.adina.spendlog.ui.viewmodel.TransactionFormViewModel

@Composable
fun SpendLogApp(viewModel: SpendLogViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomBarRoutes

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message.text,
            actionLabel = message.actionLabel,
            duration = if (message.actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        viewModel.consumeMessage(message.id)
        if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    BottomItem(navController, currentRoute, Routes.Transactions, "Entries", Icons.AutoMirrored.Filled.ListAlt)
                    NavigationBarItem(
                        selected = currentRoute == Routes.Overview,
                        onClick = { navController.navigateToTab(Routes.Overview) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.overBudgetCount > 0) Badge { Text(uiState.overBudgetCount.toString()) }
                            }) {
                                Icon(Icons.Filled.Insights, contentDescription = null)
                            }
                        },
                        label = { Text("Overview") }
                    )
                    BottomItem(navController, currentRoute, Routes.Accounts, "Accounts", Icons.Filled.AccountBalanceWallet)
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Transactions,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Transactions) {
                TransactionsScreen(
                    uiState = uiState,
                    onAddTransaction = { navController.navigate(Routes.newTransaction()) },
                    onLoadDemo = viewModel::loadDemoMonth,
                    onOpenTransaction = { id -> navController.navigate(Routes.transactionDetail(id)) }
                )
            }
            composable(Routes.Overview) {
                OverviewScreen(
                    uiState = uiState,
                    onSaveBudget = viewModel::saveBudget,
                    onDeleteBudget = viewModel::deleteBudget,
                    onOpenCategory = { category -> navController.navigate(Routes.categoryHistory(category)) }
                )
            }
            composable(Routes.Accounts) {
                AccountsScreen(
                    uiState = uiState,
                    onSaveAccount = viewModel::saveAccount,
                    onDeleteAccount = viewModel::deleteAccount
                )
            }
            composable(
                route = Routes.TransactionDetail,
                arguments = listOf(navArgument(Routes.TransactionIdArg) { type = NavType.IntType })
            ) { entry ->
                val transactionId = entry.arguments?.getInt(Routes.TransactionIdArg) ?: return@composable
                TransactionDetailScreen(
                    transactionId = transactionId,
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.editTransaction(id)) },
                    onDelete = { id ->
                        viewModel.deleteTransaction(id)
                        navController.popBackStack()
                    },
                    onOpenCategory = { category -> navController.navigate(Routes.categoryHistory(category)) }
                )
            }
            composable(
                route = Routes.TransactionForm,
                arguments = listOf(
                    navArgument(Routes.TransactionIdArg) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                val formViewModel: TransactionFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
                TransactionFormScreen(
                    viewModel = formViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { message ->
                        viewModel.showMessage(message)
                        navController.popBackStack()
                    },
                    onOpenAccounts = {
                        navController.popBackStack()
                        navController.navigateToTab(Routes.Accounts)
                    }
                )
            }
            composable(
                route = Routes.CategoryHistory,
                arguments = listOf(navArgument(Routes.CategoryArg) { type = NavType.StringType })
            ) { entry ->
                val category = entry.arguments?.getString(Routes.CategoryArg)?.let(Uri::decode) ?: return@composable
                CategoryHistoryScreen(
                    category = category,
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onOpenTransaction = { id -> navController.navigate(Routes.transactionDetail(id)) }
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomItem(
    navController: NavHostController,
    currentRoute: String?,
    route: String,
    label: String,
    icon: ImageVector
) {
    NavigationBarItem(
        selected = currentRoute == route,
        onClick = { navController.navigateToTab(route) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) }
    )
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
