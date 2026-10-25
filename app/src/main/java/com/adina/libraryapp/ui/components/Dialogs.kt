package com.adina.libraryapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.model.LibraryItem
import com.adina.libraryapp.model.Member

@Composable
fun BorrowDialog(
    item: LibraryItem,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (memberId: Int) -> Unit
) {
    var selectedMemberId by remember(item.id) { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Borrow item") },
        text = {
            Column {
                Text(text = "\"${item.title}\"", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Loan period: ${item.loanDays} days. Max 3 items per member.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                MemberPicker(
                    members = members,
                    selectedMemberId = selectedMemberId,
                    onSelect = { selectedMemberId = it }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedMemberId != null,
                onClick = { selectedMemberId?.let(onConfirm) }
            ) {
                Text("Borrow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (
        type: String,
        title: String,
        year: Int,
        author: String,
        pages: Int,
        issue: Int,
        durationMinutes: Int
    ) -> Unit
) {
    var type by remember { mutableStateOf("Book") }
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Book", "Magazine", "DVD").forEach { itemType ->
                        FilterChip(
                            selected = type == itemType,
                            onClick = { type = itemType },
                            label = { Text(itemType) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Year") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                when (type) {
                    "Book" -> {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Author") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pages,
                            onValueChange = { pages = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Pages") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    "Magazine" -> {
                        OutlinedTextField(
                            value = issue,
                            onValueChange = { issue = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Issue number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Duration (minutes)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    onAdd(
                        type,
                        title.trim(),
                        year.toIntOrNull() ?: 2024,
                        author.trim(),
                        pages.toIntOrNull() ?: 0,
                        issue.toIntOrNull() ?: 1,
                        duration.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && email.isNotBlank(),
                onClick = { onConfirm(name.trim(), email.trim()) }
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
