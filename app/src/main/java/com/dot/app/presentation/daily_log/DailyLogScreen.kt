package com.dot.app.presentation.daily_log

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dot.app.presentation.components.DotSoftIconBadge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.domain.model.Symptom
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotOutlineSoft
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    onNavigateBack: () -> Unit,
    onOpenEmergencyModule: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val fullDateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "MX"))
    }
    val chipDateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    }

    if (showDatePicker) {
        val initialMillis = uiState.selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.onDateSelected(selected)
                        }
                        showDatePicker = false
                    }
                ) { Text("Elegir", color = DotRoseStrong) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = DotDeepSlate) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = DotBlushCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Registrar síntomas",
                            style = MaterialTheme.typography.titleLarge,
                            color = DotInkBlack
                        )
                        Text(
                            text = uiState.selectedDate.format(fullDateFormatter)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = DotMutedText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = DotDeepSlate
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Cambiar fecha",
                            tint = DotRoseStrong
                        )
                    }
                    if (uiState.isExistingRecord) {
                        IconButton(onClick = viewModel::deleteCurrentDateLog) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Eliminar registro",
                                tint = DotDeepSlate
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DotWhite)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selector rápido de días recientes
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recentDates.forEach { date ->
                    val label = when (date) {
                        LocalDate.now() -> "Hoy"
                        LocalDate.now().minusDays(1) -> "Ayer"
                        else -> date.format(chipDateFormatter)
                    }
                    FilterChip(
                        selected = uiState.selectedDate == date,
                        onClick = { viewModel.onDateSelected(date) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DotRoseStrong,
                            selectedLabelColor = DotWhite,
                            containerColor = DotWhite,
                            labelColor = DotInkBlack
                        )
                    )
                }
            }

            // 1. Flujo menstrual
            LogSectionCard(
                title = "Flujo menstrual",
                icon = Icons.Outlined.WaterDrop,
                iconBg = DotBlushPink.copy(alpha = 0.72f),
                iconTint = DotRoseStrong
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowLevel.entries.forEach { level ->
                        FilterChip(
                            selected = uiState.flowLevel == level,
                            onClick = { viewModel.onFlowLevelSelected(level) },
                            label = { Text(level.toDisplayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DotRoseStrong,
                                selectedLabelColor = DotWhite,
                                containerColor = DotBlushCanvas,
                                labelColor = DotInkBlack
                            )
                        )
                    }
                }
            }

            // 2. Actividad sexual (directo: Con condón / Sin condón / Otros)
            LogSectionCard(
                title = "Actividad sexual",
                icon = Icons.Outlined.FavoriteBorder,
                iconBg = DotBlueSoft,
                iconTint = DotDeepSlate
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        Triple("No tuve", false, ProtectionMethod.NONE),
                        Triple("Sexo con condón", true, ProtectionMethod.BARRIER_CONDOM),
                        Triple("Sexo sin condón", true, ProtectionMethod.NONE),
                        Triple("Con anticonceptivo", true, ProtectionMethod.HORMONAL_ROUTINE),
                        Triple("DIU / Implante", true, ProtectionMethod.IUD_OR_IMPLANT),
                        Triple("Coito interrumpido", true, ProtectionMethod.WITHDRAWAL)
                    )
                    options.forEach { (label, hadSex, method) ->
                        val isSelected = if (!hadSex) {
                            !uiState.hadSexualActivity
                        } else {
                            uiState.hadSexualActivity && uiState.protectionUsed == method
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onQuickSexOptionSelected(hadSex, method) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DotDeepSlate,
                                selectedLabelColor = DotWhite,
                                containerColor = DotBlueSoft,
                                labelColor = DotInkBlack
                            )
                        )
                    }
                }
            }

            // 3. Anticonceptivos y Postday
            LogSectionCard(
                title = "Anticonceptivos y Postday",
                icon = Icons.Outlined.Medication,
                iconBg = DotSageSoft,
                iconTint = DotSageDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pastilla anticonceptiva diaria", style = MaterialTheme.typography.bodyLarge, color = DotInkBlack)
                        Text("¿Tomaste tu dosis de hoy?", style = MaterialTheme.typography.labelMedium, color = DotMutedText)
                    }
                    Switch(
                        checked = uiState.tookContraceptivePill,
                        onCheckedChange = viewModel::onTookContraceptivePillChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DotWhite,
                            checkedTrackColor = DotRoseStrong
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DotBlueSoft,
                    border = BorderStroke(1.dp, DotOutlineSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.onTookEmergencyPillChanged(true)
                            onOpenEmergencyModule()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HealthAndSafety,
                            contentDescription = null,
                            tint = DotDeepSlate,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Registrar pastilla del día siguiente (Postday)",
                                style = MaterialTheme.typography.labelLarge,
                                color = DotInkBlack
                            )
                            Text(
                                text = "Recalibra tu ciclo automáticamente sin alertas falsas",
                                style = MaterialTheme.typography.labelSmall,
                                color = DotDeepSlate
                            )
                        }
                    }
                }
            }

            // 4. Síntomas físicos y anímicos
            LogSectionCard(
                title = "Síntomas físicos y ánimo",
                icon = Icons.Outlined.Spa,
                iconBg = DotBlushSurface,
                iconTint = DotRoseStrong
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Symptom.entries.forEach { symptom ->
                        val selected = symptom in uiState.selectedSymptoms
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onSymptomToggled(symptom) },
                            label = { Text(symptom.toDisplayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DotRoseStrong,
                                selectedLabelColor = DotWhite,
                                containerColor = DotBlushSurface,
                                labelColor = DotInkBlack
                            )
                        )
                    }
                }
            }

            // 5. Notas opcionales
            LogSectionCard(
                title = "Notas del día",
                icon = Icons.Outlined.EditNote,
                iconBg = DotBlueSoft,
                iconTint = DotDeepSlate
            ) {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    placeholder = { Text("Ej. Dormí poco, tomé té caliente...") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.feedbackMessage?.let { feedback ->
                Surface(
                    color = DotSageSoft,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotSageDark,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Button(
                onClick = viewModel::saveDailyLog,
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DotRoseStrong,
                    contentColor = DotWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (uiState.isSaving) "Guardando..." else "Guardar registro",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LogSectionCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DotSoftIconBadge(
                    icon = icon,
                    backgroundColor = iconBg,
                    iconTint = iconTint,
                    size = 34.dp,
                    iconSize = 18.dp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
            }
            content()
        }
    }
}

fun FlowLevel.toDisplayName(): String = when (this) {
    FlowLevel.NONE -> "Sin sangrado"
    FlowLevel.SPOTTING -> "Manchado"
    FlowLevel.LIGHT -> "Ligero"
    FlowLevel.MODERATE -> "Moderado"
    FlowLevel.HEAVY -> "Abundante"
}

fun Symptom.toDisplayName(): String = when (this) {
    Symptom.CRAMPS -> "Cólicos"
    Symptom.HEADACHE -> "Dolor de cabeza"
    Symptom.BLOATING -> "Inflamación"
    Symptom.BREAST_TENDERNESS -> "Sensibilidad mamaria"
    Symptom.FATIGUE -> "Fatiga"
    Symptom.ACNE -> "Acné"
    Symptom.MOOD_CHANGES -> "Cambios de ánimo"
    Symptom.LOWER_BACK_PAIN -> "Dolor lumbar"
    Symptom.NAUSEA -> "Náuseas"
    Symptom.ANXIETY_STRESS -> "Estrés / Ansiedad"
    Symptom.CRAVINGS -> "Antojos"
    Symptom.INSOMNIA -> "Insomnio"
    Symptom.CERVICAL_MUCUS_CHANGES -> "Flujo cervical"
}
