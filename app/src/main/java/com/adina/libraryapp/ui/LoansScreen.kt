package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.LibraryUiState
import com.adina.libraryapp.ui.components.LoanCard

@Composable
fun LoansScreen(
    uiState: LibraryUiState,
    onReturn: (itemId: Int, daysKept: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Loans & fines", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(14.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Total items: ${uiState.items.size}")
                Text(
                    text = "Available: ${uiState.items.count { it.isAvailable }} · " +
                        "Borrowed: ${uiState.loans.size}"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Fines", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.finesByMember.isEmpty()) {
                    Text(
                        text = "No fines yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.finesByMember.forEach { (memberName, amount) ->
                        Text(text = "$memberName — $${"%.2f".format(amount)}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.loans.isEmpty()) {
            Text(
                text = "No active loans. Borrow something from the catalog.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.loans, key = { it.item.id }) { loan ->
                    LoanCard(
                        loan = loan,
                        onReturn = { daysKept -> onReturn(loan.item.id, daysKept) }
                    )
                }
            }
        }
    }
}
