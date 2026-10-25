package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
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
import com.adina.libraryapp.LibraryViewModel
import com.adina.libraryapp.ui.navigation.Routes

@Composable
fun LibraryApp(viewModel: LibraryViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lifecycle-aware collection: stops collecting when the UI is not STARTED
    // (e.g. Activity paused). Survives rotation because the ViewModel does.
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
                        selected = currentRoute == Routes.Catalog,
                        onClick = {
                            navController.navigate(Routes.Catalog) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                        label = { Text("Catalog") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.Loans,
                        onClick = {
                            navController.navigate(Routes.Loans) {
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
                                    if (uiState.loans.isNotEmpty()) {
                                        Badge { Text(uiState.loans.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Receipt, contentDescription = null)
                            }
                        },
                        label = { Text("Loans") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.Members,
                        onClick = {
                            navController.navigate(Routes.Members) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.People, contentDescription = null) },
                        label = { Text("Members") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Catalog,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Catalog) {
                CatalogScreen(
                    uiState = uiState,
                    onBorrow = viewModel::borrow,
                    onAddItem = viewModel::addItem,
                    onLoadArrivals = viewModel::loadNewArrivals,
                    onOpenItem = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId))
                    }
                )
            }
            composable(Routes.Loans) {
                LoansScreen(
                    uiState = uiState,
                    onReturn = viewModel::returnItem
                )
            }
            composable(Routes.Members) {
                MembersScreen(
                    uiState = uiState,
                    onAddMember = viewModel::addMember
                )
            }
            composable(
                route = Routes.ItemDetail,
                arguments = listOf(navArgument("itemId") { type = NavType.IntType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
                ItemDetailScreen(
                    itemId = itemId,
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onBorrow = viewModel::borrow
                )
            }
        }
    }
}
