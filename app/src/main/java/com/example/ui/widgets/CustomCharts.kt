package com.example.ui.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// Vibrant palette to use for dynamic category and comparison colors
val ChartPalette = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFFEC4899), // Pink
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6), // Violet
    Color(0xFFEF4444), // Red
    Color(0xFF14B8A6), // Teal
    Color(0xFFF97316), // Orange
    Color(0xFF6B7280)  // Gray
)

@Composable
fun CustomPieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (data.isEmpty() || data.values.all { it <= 0.0 }) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No data to display in Pie Chart",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = data.values.sum()
    val slices = data.entries.mapIndexed { index, entry ->
        val percentage = (entry.value / total).toFloat()
        PieSlice(
            label = entry.key,
            value = entry.value,
            percentage = percentage,
            color = ChartPalette[index % ChartPalette.size]
        )
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Pie Canvas
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = slice.percentage * 360f * animProgress.value
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                    )
                    startAngle += slice.percentage * 360f
                }
            }

            // Central Summary Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.getDefault(), "%s%.0f", currencySymbol, total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Legend details list
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.take(5).forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slice.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%% (%s%.1f)", slice.percentage * 100, currencySymbol, slice.value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (slices.size > 5) {
                Text(
                    text = "+ ${slices.size - 5} other categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }
        }
    }
}

data class PieSlice(
    val label: String,
    val value: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun CustomBarChart(
    data: Map<String, Double>, // xLabel -> Amount
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (data.isEmpty() || data.values.all { it <= 0.0 }) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No data to display in Bar Chart",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxAmount = remember(data) { data.values.maxOrNull() ?: 1.0 }
    val keys = data.keys.toList()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animProgress.animateTo(1f, animationSpec = tween(900))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.entries.forEachIndexed { index, entry ->
                val ratio = (entry.value / maxAmount).toFloat()
                val barColor = ChartPalette[index % ChartPalette.size]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f", entry.value),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio * animProgress.value)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        barColor,
                                        barColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            keys.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CustomTrendLineChart(
    points: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (points.isEmpty() || points.all { it <= 0.0 }) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No structured transaction trends",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxVal = remember(points) { points.maxOrNull()?.toFloat() ?: 1.0f }
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.animateTo(1f, animationSpec = tween(1200))
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1).coerceAtLeast(1)

            val path = Path()
            val gradientPath = Path()

            points.forEachIndexed { i, dVal ->
                val x = i * spacing
                val ratio = dVal.toFloat() / maxVal
                val y = height - (ratio * height * animProgress.value)

                if (i == 0) {
                    path.moveTo(x, y)
                    gradientPath.moveTo(x, height)
                    gradientPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    gradientPath.lineTo(x, y)
                }

                if (i == points.size - 1) {
                    gradientPath.lineTo(x, height)
                    gradientPath.close()
                }
            }

            // Draw area gradient
            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.0f)
                    )
                )
            )

            // Draw trend line
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points circles
            points.forEachIndexed { i, dVal ->
                val x = i * spacing
                val ratio = dVal.toFloat() / maxVal
                val y = height - (ratio * height * animProgress.value)
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, lbl ->
                Text(
                    text = lbl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
