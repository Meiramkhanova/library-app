package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.LibraryUiState
import com.adina.libraryapp.model.Book
import com.adina.libraryapp.model.Dvd
import com.adina.libraryapp.model.Magazine
import com.adina.libraryapp.ui.components.BorrowDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    uiState: LibraryUiState,
    onBack: () -> Unit,
    onBorrow: (itemId: Int, memberId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = uiState.items.find { it.id == itemId }
    var showBorrowDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Item details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (item == null) {
            Text(
                text = "Item #$itemId was not found.",
                modifier = Modifier.padding(16.dp)
            )
            return
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = item.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${item.type} · ${item.year}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Availability", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.isAvailable) "Available to borrow" else "Currently borrowed",
                        color = if (item.isAvailable) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Loan period: ${item.loanDays} days")
                    Text(
                        text = when (item) {
                            is Book -> "Late fee: $0.50 / day"
                            is Magazine -> "Late fee: $0.25 / day"
                            is Dvd -> "Late fee: $1.00 / day"
                            else -> "Late fee: varies"
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    when (item) {
                        is Book -> {
                            Text("Author: ${item.author}")
                            Text("Pages: ${item.pages}")
                        }
                        is Magazine -> Text("Issue: #${item.issue}")
                        is Dvd -> Text("Duration: ${item.durationMinutes} minutes")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.describe(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showBorrowDialog = true },
                enabled = item.isAvailable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (item.isAvailable) "Borrow this item" else "Already borrowed")
            }
        }
    }

    if (showBorrowDialog && item != null) {
        BorrowDialog(
            item = item,
            members = uiState.members,
            onDismiss = { showBorrowDialog = false },
            onConfirm = { memberId ->
                onBorrow(item.id, memberId)
                showBorrowDialog = false
            }
        )
    }
}
