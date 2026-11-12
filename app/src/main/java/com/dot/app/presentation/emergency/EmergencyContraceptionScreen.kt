package com.dot.app.presentation.emergency

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.EmergencyCompoundType
import com.dot.app.domain.model.EmergencyContraceptionLog
import com.dot.app.domain.model.EmpatheticMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmergencyContraceptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmergencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "MX"))
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    if (showDatePicker) {
        val initialMillis = uiState.takenDate
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
                            viewModel.onTakenDateChanged(selected)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
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
                    Text(
                        text = "Anticoncepción de emergencia",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
            // Tarjeta informativa posterior al guardado con el recálculo contextual (RF-03, RF-04, RF-06)
            AnimatedVisibility(visible = uiState.showPostSaveInfoCard) {
                PostSaveContextualImpactCard(
                    prediction = uiState.recalculatedPrediction,
                    empatheticMessage = uiState.empatheticMessage,
                    dateFormatter = dateFormatter,
                    onReturnHome = onNavigateBack
                )
            }

            // Encabezado empático
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (uiState.editingEventId != 0L) {
                            "Editando registro de pastilla de emergencia"
                        } else {
                            "Registra tu toma sin juicios ni alarmas"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La anticoncepción de emergencia actúa posponiendo la ovulación. Al registrarla, Dot ajustará automáticamente tu ventana esperada para no mostrarte alertas de retraso irreales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // 1. Fecha y hora aproximada de la toma
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
                        text = "Fecha y hora aproximada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.takenDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text("Elegir fecha")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Hoy" to LocalDate.now(),
                            "Ayer" to LocalDate.now().minusDays(1),
                            "Hace 2 días" to LocalDate.now().minusDays(2)
                        ).forEach { (label, date) ->
                            FilterChip(
                                selected = uiState.takenDate == date,
                                onClick = { viewModel.onTakenDateChanged(date) },
                                label = { Text(label) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registrar hora aproximada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.includeApproximateTime,
                            onCheckedChange = { viewModel.onIncludeTimeChanged(it) }
                        )
                    }

                    if (uiState.includeApproximateTime) {
                        Text(
                            text = "Hora aproximada: ${uiState.approximateTime.format(timeFormatter)} hrs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Slider(
                            value = uiState.approximateTime.hour.toFloat(),
                            onValueChange = {
                                viewModel.onApproximateTimeChanged(
                                    it.roundToInt(),
                                    0
                                )
                            },
                            valueRange = 0f..23f,
                            steps = 22
                        )
                    }
                }
            }

            // 2. Tipo de pastilla / compuesto activo (RF-03, RF-06)
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
                        text = "Tipo de compuesto activo (si lo conoces)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cada compuesto tiene un margen de acción distinto sobre la ovulación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmergencyCompoundType.entries.forEach { compound ->
                            FilterChip(
                                selected = uiState.compoundType == compound,
                                onClick = { viewModel.onCompoundTypeSelected(compound) },
                                label = { Text(compound.toDisplayLabel()) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Text(
                        text = uiState.compoundType.toEducationalHint(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Protección previa y relaciones posteriores (RF-03)
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
                    Text(
                        text = "Contexto de protección",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "¿Se utilizó protección previa?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Por ejemplo, uso de condón con ruptura o deslizamiento, u olvido de pastilla regular.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.priorProtectionUsed,
                            onCheckedChange = { viewModel.onPriorProtectionChanged(it) }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "¿Hubo relaciones posteriores a la toma?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "La pastilla pospone la ovulación temporalmente; es importante usar un método de barrera el resto del ciclo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.hadSubsequentIntercourse,
                            onCheckedChange = { viewModel.onSubsequentIntercourseChanged(it) }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        label = { Text("Notas opcionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            uiState.validationMessage?.let { message ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.saveEmergencyContraceptionEvent() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when {
                        uiState.isSaving -> "Recalibrando proyección..."
                        uiState.editingEventId != 0L -> "Actualizar registro y recalcular"
                        else -> "Guardar y recalcular ciclo"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Historial de eventos registrados para edición/eliminación (RF-08)
            if (uiState.recordedEvents.isNotEmpty()) {
                RecordedEmergencyEventsSection(
                    events = uiState.recordedEvents,
                    dateFormatter = dateFormatter,
                    onEdit = { viewModel.selectEventForEdit(it) },
                    onDelete = { viewModel.deleteEmergencyEvent(it.id) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PostSaveContextualImpactCard(
    prediction: CyclePrediction?,
    empatheticMessage: EmpatheticMessage?,
    dateFormatter: DateTimeFormatter,
    onReturnHome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Ciclo recalibrado • Qué esperar en tu periodo (RF-06)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            prediction?.let {
                Text(
                    text = "Nueva ventana estimada: ${it.estimatedWindowStart.format(dateFormatter)} al ${it.estimatedWindowEnd.format(dateFormatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            empatheticMessage?.let { msg ->
                Text(
                    text = msg.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Cambios temporales esperables:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Tu próximo sangrado puede adelantarse unos días o retrasarse hasta una semana respecto a tu fecha habitual.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Es común presentar manchado intermenstrual leve (spotting), sensibilidad mamaria o fatiga transitoria.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Este ciclo se marcará como atípico para no distorsionar tus promedios futuros.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedButton(
                onClick = onReturnHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al panel principal")
            }
        }
    }
}

@Composable
private fun RecordedEmergencyEventsSection(
    events: List<EmergencyContraceptionLog>,
    dateFormatter: DateTimeFormatter,
    onEdit: (EmergencyContraceptionLog) -> Unit,
    onDelete: (EmergencyContraceptionLog) -> Unit
) {
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
            Text(
                text = "Tomas registradas (RF-08)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            events.forEach { event ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.takenDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = event.compoundType.toDisplayLabel(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            TextButton(onClick = { onEdit(event) }) {
                                Text("Editar")
                            }
                            IconButton(onClick = { onDelete(event) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar registro"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun EmergencyCompoundType.toDisplayLabel(): String = when (this) {
    EmergencyCompoundType.LEVONORGESTREL -> "Levonorgestrel (1.5 mg)"
    EmergencyCompoundType.ULIPRISTAL_ACETATE -> "Acetato de ulipristal (30 mg)"
    EmergencyCompoundType.UNKNOWN -> "No estoy segura / Otra"
}

private fun EmergencyCompoundType.toEducationalHint(): String = when (this) {
    EmergencyCompoundType.LEVONORGESTREL ->
        "Levonorgestrel: es la pastilla de emergencia más común (efectiva hasta 72 h). Suele desplazar la ventana menstrual hasta 7 días."
    EmergencyCompoundType.ULIPRISTAL_ACETATE ->
        "Acetato de ulipristal: efectiva hasta 120 h (5 días). Puede posponer la ovulación incluso cerca del pico hormonal, desplazando el periodo hasta 10 días."
    EmergencyCompoundType.UNKNOWN ->
        "Si no conoces el compuesto exacto, Dot aplicará un margen flexible de seguridad de hasta 7 días sobre tu fecha estimada."
}
