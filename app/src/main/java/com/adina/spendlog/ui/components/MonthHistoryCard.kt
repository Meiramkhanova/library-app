package com.adina.spendlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adina.spendlog.model.MonthSummary

@Composable
fun MonthHistoryCard(
    history: List<MonthSummary>,
    modifier: Modifier = Modifier
) {
    val max = history.maxOfOrNull { maxOf(it.income, it.expense) }?.takeIf { it > 0 } ?: 1.0

    SurfaceCard(modifier = modifier) {
        Text(text = "Last 6 months", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend(color = MaterialTheme.colorScheme.secondary, label = "Income")
            Legend(color = MaterialTheme.colorScheme.error, label = "Expenses")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            history.forEach { month ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.height(96.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Bar(fraction = (month.income / max).toFloat(), color = MaterialTheme.colorScheme.secondary)
                        Bar(fraction = (month.expense / max).toFloat(), color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = month.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Bar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(10.dp)
            .fillMaxHeight(fraction.coerceIn(0.03f, 1f))
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(color)
    )
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
