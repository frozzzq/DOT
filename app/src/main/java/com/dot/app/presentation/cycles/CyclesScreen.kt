package com.dot.app.presentation.cycles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.CycleClassification
import com.dot.app.domain.model.CycleHealthAnalysis
import com.dot.app.domain.model.EvaluatedCycle
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDustyBlue
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageGreen
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CyclesScreen(
    viewModel: CyclesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX")) }

    if (uiState.showAddCycleDialog) {
        AddPastCycleDialog(
            onDismiss = { viewModel.onShowAddCycleDialog(false) },
            onSave = { start, cycleLen, bleedLen, postday ->
                viewModel.addHistoricalCycle(start, cycleLen, bleedLen, postday)
            }
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DotBlushCanvas),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DotRoseStrong)
        }
        return
    }

    val analysis = uiState.analysis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DotBlushCanvas)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DotBlushPink.copy(alpha = 0.45f))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mis ciclos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DotInkBlack
                )
                Text(
                    text = "Historial, regularidad y orientación clínica.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DotDeepSlate
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = DotRoseStrong,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { viewModel.onShowAddCycleDialog(true) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = DotWhite, modifier = Modifier.size(18.dp))
                    Text("Agregar", style = MaterialTheme.typography.labelLarge, color = DotWhite)
                }
            }
        }

        if (analysis != null) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Métricas resumen
                item {
                    CycleMetricsRow(analysis = analysis)
                }

                // 2. Tarjeta de evaluación clínica y recomendación de especialista
                item {
                    SpecialistEvaluationCard(analysis = analysis)
                }

                // 3. Título del historial
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de ciclos (${analysis.evaluatedCycles.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = DotInkBlack
                        )
                        Text(
                            text = "Normal: 24–38 días",
                            style = MaterialTheme.typography.labelSmall,
                            color = DotMutedText
                        )
                    }
                }

                // 4. Lista cronológica de ciclos
                items(
                    items = analysis.evaluatedCycles,
                    key = { "${it.cycle.id}_${it.cycle.startDate}" }
                ) { evaluated ->
                    CycleHistoryItemCard(
                        evaluated = evaluated,
                        dateFormatter = dateFormatter,
                        canDelete = analysis.evaluatedCycles.size > 1,
                        onDelete = { viewModel.deleteCycle(evaluated.cycle.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun CycleMetricsRow(analysis: CycleHealthAnalysis) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricBox(
            value = "${analysis.averageCycleLengthDays} d",
            label = "Ciclo prom.",
            accentColor = DotRoseStrong,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            value = "${analysis.averageBleedingDays} d",
            label = "Sangrado",
            accentColor = DotDeepSlate,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            value = "±${analysis.cycleVariationDays} d",
            label = "Variación",
            accentColor = DotSageDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricBox(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = DotMutedText
            )
        }
    }
}

@Composable
private fun SpecialistEvaluationCard(analysis: CycleHealthAnalysis) {
    val (bgColor, badgeBg, badgeText, icon) = when (analysis.overallClassification) {
        CycleClassification.ANOMALOUS -> QuadColor(
            DotBlushPink.copy(alpha = 0.72f),
            DotRoseStrong,
            "Revisar con especialista",
            Icons.Outlined.MedicalServices
        )
        CycleClassification.IRREGULAR -> QuadColor(
            DotBlueSoft,
            DotDeepSlate,
            "Ciclo irregular",
            Icons.Outlined.Timeline
        )
        else -> QuadColor(
            DotSageSoft,
            DotSageDark,
            "Normal y regular",
            Icons.Outlined.CheckCircle
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = DotWhite, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(
                        text = badgeText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DotDeepSlate,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysis.statusHeadline,
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                }
            }

            Text(
                text = analysis.clinicalSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = DotInkBlack
            )

            if (analysis.shouldConsultSpecialist && analysis.specialistReasons.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DotWhite.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "¿Por qué sugerimos consultar a una ginecóloga?",
                            style = MaterialTheme.typography.labelLarge,
                            color = DotRoseStrong
                        )
                        analysis.specialistReasons.forEach { reason ->
                            Text(
                                text = "• $reason",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DotInkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class QuadColor(
    val bg: Color,
    val badgeBg: Color,
    val badgeLabel: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun CycleHistoryItemCard(
    evaluated: EvaluatedCycle,
    dateFormatter: DateTimeFormatter,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val (chipLabel, chipBg, chipText) = when (evaluated.classification) {
        CycleClassification.NORMAL_REGULAR -> Triple("Normal / Regular", DotSageSoft, DotSageDark)
        CycleClassification.IRREGULAR -> Triple("Irregular", DotBlueSoft, DotDeepSlate)
        CycleClassification.ANOMALOUS -> Triple("Anómalo", DotRoseStrong, DotWhite)
        CycleClassification.ALTERED_BY_EMERGENCY -> Triple("Alterado por Postday", DotDustyBlue, DotInkBlack)
        CycleClassification.IN_PROGRESS -> Triple("En curso", DotBlushPink, DotInkBlack)
    }

    val startLabel = evaluated.cycle.startDate.format(dateFormatter)
    val endLabel = evaluated.cycle.endDate?.format(dateFormatter) ?: "Actual"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$startLabel — $endLabel",
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = "${evaluated.effectiveLengthDays} días de ciclo • ${evaluated.effectiveBleedingDays} días de sangrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotMutedText
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar ciclo",
                            tint = DotMutedText
                        )
                    }
                }
            }

            // Barra visual proporcional (sangrado en #B77076 dentro de la duración del ciclo)
            val bleedingFraction = (evaluated.effectiveBleedingDays.toFloat() /
                evaluated.effectiveLengthDays.coerceAtLeast(1).toFloat()).coerceIn(0.08f, 1f)
            LinearProgressIndicator(
                progress = { bleedingFraction },
                color = DotRoseStrong,
                trackColor = DotBlushSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = chipBg
                ) {
                    Text(
                        text = chipLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = chipText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = evaluated.clinicalNote,
                style = MaterialTheme.typography.labelMedium,
                color = DotDeepSlate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPastCycleDialog(
    onDismiss: () -> Unit,
    onSave: (LocalDate, Int, Int, Boolean) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(38)) }
    var cycleLength by remember { mutableIntStateOf(29) }
    var bleedingDays by remember { mutableIntStateOf(5) }
    var alteredByEmergency by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX")) }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            if (!picked.isAfter(LocalDate.now())) startDate = picked
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar", color = DotRoseStrong) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = DotDeepSlate) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DotWhite,
        title = { Text("Registrar ciclo anterior", style = MaterialTheme.typography.titleLarge, color = DotInkBlack) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Inicio de menstruación", style = MaterialTheme.typography.labelMedium, color = DotMutedText)
                        Text(startDate.format(formatter), style = MaterialTheme.typography.titleMedium, color = DotRoseStrong)
                    }
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("Cambiar", color = DotRoseStrong)
                    }
                }

                CompactDialogStepper(
                    label = "Duración total del ciclo",
                    value = cycleLength,
                    onDec = { cycleLength = (cycleLength - 1).coerceAtLeast(15) },
                    onInc = { cycleLength = (cycleLength + 1).coerceAtMost(100) }
                )

                CompactDialogStepper(
                    label = "Días de sangrado",
                    value = bleedingDays,
                    onDec = { bleedingDays = (bleedingDays - 1).coerceAtLeast(1) },
                    onInc = { bleedingDays = (bleedingDays + 1).coerceAtMost(14) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Se tomó Postday en este ciclo?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotDeepSlate,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = alteredByEmergency,
                        onCheckedChange = { alteredByEmergency = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DotWhite,
                            checkedTrackColor = DotRoseStrong
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(startDate, cycleLength, bleedingDays, alteredByEmergency) },
                colors = ButtonDefaults.buttonColors(containerColor = DotRoseStrong, contentColor = DotWhite)
            ) {
                Text("Guardar", color = DotWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = DotDeepSlate) }
        }
    )
}

@Composable
private fun CompactDialogStepper(
    label: String,
    value: Int,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DotInkBlack)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Remove, contentDescription = null, tint = DotDeepSlate)
            }
            Text("$value d", style = MaterialTheme.typography.titleMedium, color = DotRoseStrong)
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = DotDeepSlate)
            }
        }
    }
}
