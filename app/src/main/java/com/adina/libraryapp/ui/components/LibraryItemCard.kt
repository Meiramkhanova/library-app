package com.adina.libraryapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.adina.libraryapp.model.Book
import com.adina.libraryapp.model.Dvd
import com.adina.libraryapp.model.LibraryItem
import com.adina.libraryapp.model.Magazine

@Composable
fun LibraryItemCard(
    item: LibraryItem,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onBorrow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = iconFor(item),
                        contentDescription = item.type,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = detailsFor(item),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(isAvailable = item.isAvailable)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.describe(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Loan period: ${item.loanDays} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggleExpanded) {
                    Text(if (expanded) "Hide details" else "Details")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onBorrow, enabled = item.isAvailable) {
                    Text("Borrow")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(isAvailable: Boolean) {
    val background = if (isAvailable) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isAvailable) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isAvailable) "Available" else "Borrowed",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

private fun iconFor(item: LibraryItem): ImageVector = when (item) {
    is Book -> Icons.Filled.MenuBook
    is Magazine -> Icons.Filled.Newspaper
    is Dvd -> Icons.Filled.Movie
    else -> Icons.Filled.MenuBook
}

private fun detailsFor(item: LibraryItem): String = when (item) {
    is Book -> "${item.author} · ${item.pages} pages · ${item.year}"
    is Magazine -> "Issue #${item.issue} · ${item.year}"
    is Dvd -> "${item.durationMinutes} min · ${item.year}"
    else -> item.year.toString()
}
