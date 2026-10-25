package com.adina.libraryapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.LibraryUiState
import com.adina.libraryapp.ui.components.AddMemberDialog
import com.adina.libraryapp.ui.components.MemberCard

@Composable
fun MembersScreen(
    uiState: LibraryUiState,
    onAddMember: (name: String, email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dialog visibility is local UI state — remember is enough (lost on rotation is fine).
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Members", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${uiState.members.size} registered readers · max 3 loans each",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(onClick = { showAddDialog = true }) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Register member")
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Fines summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (uiState.finesByMember.isEmpty()) {
                    Text(
                        text = "No outstanding fines",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.finesByMember.forEach { (name, amount) ->
                        Text(text = "$name — $${"%.2f".format(amount)}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.members, key = { it.id }) { member ->
                MemberCard(
                    member = member,
                    activeLoans = uiState.loans.count { it.member.id == member.id },
                    fine = uiState.finesByMember[member.name] ?: 0.0,
                    borrowedTitles = uiState.loans
                        .filter { it.member.id == member.id }
                        .map { it.item.title }
                )
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email ->
                onAddMember(name, email)
                showAddDialog = false
            }
        )
    }
}
