package com.dot.app.presentation.daily_log

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.domain.model.Symptom
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val fullDateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", Locale("es", "MX"))
    }
    val chipDateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    }

    if (showDatePicker) {
        val initialMillis = uiState.selectedDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onDateSelected(selected)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Seleccionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isExistingRecord) "Editar registro diario" else "Registro diario",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = uiState.selectedDate.format(fullDateFormatter)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar otra fecha"
                        )
                    }
                    if (uiState.isExistingRecord) {
                        IconButton(onClick = { viewModel.deleteCurrentDateLog() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar registro de esta fecha"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector rápido de fecha para consultar o editar días previos (RF-08)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Text(
                            text = "Fecha del registro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Abrir calendario")
                        }
                    }

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
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 1. Nivel de flujo menstrual (RF-02)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Flujo menstrual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Selecciona la intensidad de sangrado o manchado para esta fecha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 2. Síntomas físicos y anímicos (RF-02)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Síntomas físicos y estado de ánimo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Puedes seleccionar todos los que hayas experimentado en el día.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Symptom.entries.forEach { symptom ->
                            val isSelected = symptom in uiState.selectedSymptoms
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onSymptomToggled(symptom) },
                                label = { Text(symptom.toDisplayName()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3. Actividad sexual y método de protección (RF-02)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Actividad sexual",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "¿Tuviste relaciones sexuales en esta fecha?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.hadSexualActivity,
                            onCheckedChange = { viewModel.onSexualActivityChanged(it) }
                        )
                    }

                    AnimatedVisibility(visible = uiState.hadSexualActivity) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Método de protección utilizado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProtectionMethod.entries.forEach { method ->
                                    FilterChip(
                                        selected = uiState.protectionUsed == method,
                                        onClick = { viewModel.onProtectionMethodSelected(method) },
                                        label = { Text(method.toDisplayName()) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        label = { Text("Notas personales del día (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Mensaje de confirmación o aviso
            uiState.feedbackMessage?.let { feedback ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isExistingRecord) {
                    OutlinedButton(
                        onClick = { viewModel.deleteCurrentDateLog() },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Eliminar")
                    }
                }

                Button(
                    onClick = { viewModel.saveDailyLog() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .weight(2f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = when {
                            uiState.isSaving -> "Guardando..."
                            uiState.isExistingRecord -> "Actualizar registro"
                            else -> "Guardar registro"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun FlowLevel.toDisplayName(): String = when (this) {
    FlowLevel.NONE -> "Sin sangrado"
    FlowLevel.SPOTTING -> "Manchado (Spotting)"
    FlowLevel.LIGHT -> "Flujo ligero"
    FlowLevel.MODERATE -> "Flujo moderado"
    FlowLevel.HEAVY -> "Flujo abundante"
}

fun Symptom.toDisplayName(): String = when (this) {
    Symptom.CRAMPS -> "Cólicos"
    Symptom.HEADACHE -> "Dolor de cabeza"
    Symptom.BLOATING -> "Inflamación abdominal"
    Symptom.BREAST_TENDERNESS -> "Sensibilidad mamaria"
    Symptom.FATIGUE -> "Fatiga / Cansancio"
    Symptom.ACNE -> "Acné / Piel sensible"
    Symptom.MOOD_CHANGES -> "Cambios de ánimo"
    Symptom.LOWER_BACK_PAIN -> "Dolor lumbar"
    Symptom.NAUSEA -> "Náuseas"
}

fun ProtectionMethod.toDisplayName(): String = when (this) {
    ProtectionMethod.NONE -> "Sin protección"
    ProtectionMethod.BARRIER_CONDOM -> "Condón / Barrera"
    ProtectionMethod.HORMONAL_ROUTINE -> "Anticonceptivo hormonal regular"
    ProtectionMethod.IUD_OR_IMPLANT -> "DIU o Implante"
    ProtectionMethod.WITHDRAWAL -> "Coito interrumpido"
    ProtectionMethod.OTHER -> "Otro método"
}
