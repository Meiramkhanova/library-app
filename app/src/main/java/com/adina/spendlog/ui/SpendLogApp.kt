package com.adina.spendlog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adina.spendlog.SpendLogViewModel
import com.adina.spendlog.ui.navigation.Routes

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
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.Transactions,
                        onClick = {
                            navController.navigate(Routes.Transactions) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null)
                        },
                        label = { Text("Entries") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.Overview,
                        onClick = {
                            navController.navigate(Routes.Overview) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val over = uiState.categorySpend.count { it.isOverBudget }
                                    if (over > 0) {
                                        Badge { Text(over.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Insights, contentDescription = null)
                            }
                        },
                        label = { Text("Overview") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.Accounts,
                        onClick = {
                            navController.navigate(Routes.Accounts) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
                        },
                        label = { Text("Accounts") }
                    )
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
                    onAddTransaction = viewModel::addTransaction,
                    onLoadDemo = viewModel::loadDemoMonth,
                    onOpenTransaction = { id ->
                        navController.navigate(Routes.transactionDetail(id))
                    }
                )
            }
            composable(Routes.Overview) {
                OverviewScreen(uiState = uiState)
            }
            composable(Routes.Accounts) {
                AccountsScreen(
                    uiState = uiState,
                    onAddAccount = viewModel::addAccount
                )
            }
            composable(
                route = Routes.TransactionDetail,
                arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getInt("transactionId")
                    ?: return@composable
                TransactionDetailScreen(
                    transactionId = transactionId,
                    uiState = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
