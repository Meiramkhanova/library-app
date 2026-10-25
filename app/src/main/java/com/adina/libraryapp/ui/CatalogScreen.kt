package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.LibraryUiState
import com.adina.libraryapp.model.LibraryItem
import com.adina.libraryapp.ui.components.AddItemDialog
import com.adina.libraryapp.ui.components.BorrowDialog
import com.adina.libraryapp.ui.components.FilterBar
import com.adina.libraryapp.ui.components.LibraryItemCard

/**
 * Catalog UI only. Filter/search state lives here (remember / rememberSaveable).
 * Domain actions are passed in as callbacks — state hoisting from ViewModel owner.
 */
@Composable
fun CatalogScreen(
    uiState: LibraryUiState,
    onBorrow: (itemId: Int, memberId: Int) -> Unit,
    onAddItem: (
        type: String,
        title: String,
        year: Int,
        author: String,
        pages: Int,
        issue: Int,
        durationMinutes: Int
    ) -> Unit,
    onLoadArrivals: () -> Unit,
    onOpenItem: (itemId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // rememberSaveable: survives Activity recreation (rotation).
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var onlyAvailable by rememberSaveable { mutableStateOf(false) }

    // remember only: intentionally lost on rotation — compare with search field.
    var expandedItemId by remember { mutableStateOf<Int?>(null) }

    var borrowTarget by remember { mutableStateOf<LibraryItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val visibleItems = uiState.items.filter { item ->
        (selectedType == "All" || item.type == selectedType) &&
            (!onlyAvailable || item.isAvailable) &&
            item.title.contains(query, ignoreCase = true)
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "City Library", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${uiState.items.count { it.isAvailable }} of ${uiState.items.size} items available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        FilterBar(
            query = query,
            onQueryChange = { query = it },
            selectedType = selectedType,
            onTypeChange = { selectedType = it },
            onlyAvailable = onlyAvailable,
            onOnlyAvailableChange = { onlyAvailable = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add item")
            }
            OutlinedButton(
                onClick = onLoadArrivals,
                enabled = !uiState.isLoadingArrivals
            ) {
                if (uiState.isLoadingArrivals) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Download, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("New arrivals")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (visibleItems.isEmpty()) {
            Text(
                text = "Nothing found. Try another search or add a new item.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibleItems, key = { it.id }) { item ->
                    LibraryItemCard(
                        item = item,
                        expanded = expandedItemId == item.id,
                        onToggleExpanded = {
                            expandedItemId = if (expandedItemId == item.id) null else item.id
                        },
                        onOpen = { onOpenItem(item.id) },
                        onBorrow = { borrowTarget = item }
                    )
                }
            }
        }
    }

    borrowTarget?.let { item ->
        BorrowDialog(
            item = item,
            members = uiState.members,
            onDismiss = { borrowTarget = null },
            onConfirm = { memberId ->
                onBorrow(item.id, memberId)
                borrowTarget = null
            }
        )
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, title, year, author, pages, issue, duration ->
                onAddItem(type, title, year, author, pages, issue, duration)
                showAddDialog = false
            }
        )
    }
}
