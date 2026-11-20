package com.adina.spendlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.CategorySpend
import com.adina.spendlog.model.formatMoney
import kotlin.math.roundToInt

@Composable
fun BudgetCard(
    spend: CategorySpend,
    daysLeft: Int,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = budgetStatusColor(spend)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onOpen),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = spend.category,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (spend.hasBudget) {
                    StatusPill(text = budgetStatusLabel(spend), color = statusColor)
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit ${spend.category} budget")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Delete ${spend.category} budget",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            val limit = spend.limit
            if (limit == null) {
                Text(
                    text = "Spent ${formatMoney(spend.spent)} this month · no budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onSetBudget) { Text("Set budget") }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatMoney(spend.spent)} of ${formatMoney(limit)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { spend.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (spend.isOverBudget) {
                        "Over by ${formatMoney(-spend.remaining)}"
                    } else {
                        "${formatMoney(spend.remaining)} left · $daysLeft days to go"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (spend.isOverBudget) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun budgetStatusColor(spend: CategorySpend): Color = when {
    spend.isOverBudget -> MaterialTheme.colorScheme.error
    spend.isNearLimit -> Color(0xFFF4A340)
    else -> MaterialTheme.colorScheme.primary
}

fun budgetStatusLabel(spend: CategorySpend): String = when {
    spend.isOverBudget -> "Over"
    spend.isNearLimit -> "${(spend.progress * 100).roundToInt()}%"
    else -> "On track"
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
    }
    Spacer(modifier = Modifier.width(4.dp))
}
