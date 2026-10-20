package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adina.libraryapp.LibraryViewModel

@Composable
fun LibraryApp(viewModel: LibraryViewModel) {
    // rememberSaveable: the selected tab is written to the Bundle,
    // so it survives Activity recreation (rotation).
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = null) },
                    label = { Text("Catalog") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
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
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier.padding(paddingValues)
        if (selectedTab == 0) {
            CatalogScreen(uiState = uiState, viewModel = viewModel, modifier = contentModifier)
        } else {
            LoansScreen(uiState = uiState, viewModel = viewModel, modifier = contentModifier)
        }
    }
}
