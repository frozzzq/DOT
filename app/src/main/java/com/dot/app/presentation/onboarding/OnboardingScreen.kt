package com.dot.app.presentation.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.CycleRegularity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "MX"))
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingFinished()
        }
    }

    if (showDatePicker) {
        val initialMillis = uiState.lastPeriodStartDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onLastPeriodDateChanged(selected)
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
                    Column {
                        Text(
                            text = "Bienvenida a Dot",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Tu espacio seguro, privado y sin alarmas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            // Nota de privacidad empática (RNF-01, RNF-03)
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configura tu línea base fisiológica",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estos datos se cifran y guardan únicamente en tu dispositivo para calcular ventanas flexibles adaptadas a tu cuerpo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 1. Fecha del último periodo
            OnboardingSectionCard(
                title = "1. ¿Cuándo inició tu último periodo?",
                subtitle = "Selecciona el primer día de sangrado menstrual."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.lastPeriodStartDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("Cambiar fecha")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Hoy" to LocalDate.now(),
                        "Hace 7 días" to LocalDate.now().minusDays(7),
                        "Hace 14 días" to LocalDate.now().minusDays(14),
                        "Hace 21 días" to LocalDate.now().minusDays(21)
                    ).forEach { (label, presetDate) ->
                        FilterChip(
                            selected = uiState.lastPeriodStartDate == presetDate,
                            onClick = { viewModel.onLastPeriodDateChanged(presetDate) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 2. Duración habitual del sangrado
            OnboardingSectionCard(
                title = "2. Duración habitual del sangrado",
                subtitle = "¿Cuántos días suele durar tu periodo menstrual?"
            ) {
                Text(
                    text = "${uiState.averagePeriodDuration} días",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = uiState.averagePeriodDuration.toFloat(),
                    onValueChange = { viewModel.onPeriodDurationChanged(it.roundToInt()) },
                    valueRange = OnboardingViewModel.MIN_BLEEDING_DAYS.toFloat()..OnboardingViewModel.MAX_BLEEDING_DAYS.toFloat(),
                    steps = OnboardingViewModel.MAX_BLEEDING_DAYS - OnboardingViewModel.MIN_BLEEDING_DAYS - 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${OnboardingViewModel.MIN_BLEEDING_DAYS} día",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${OnboardingViewModel.MAX_BLEEDING_DAYS} días",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Duración promedio del ciclo
            OnboardingSectionCard(
                title = "3. Duración promedio de tu ciclo",
                subtitle = "Días entre el inicio de un periodo y el inicio del siguiente (lo habitual es de 24 a 35 días)."
            ) {
                Text(
                    text = "${uiState.averageCycleLength} días",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = uiState.averageCycleLength.toFloat(),
                    onValueChange = { viewModel.onCycleLengthChanged(it.roundToInt()) },
                    valueRange = OnboardingViewModel.MIN_CYCLE_DAYS.toFloat()..OnboardingViewModel.MAX_CYCLE_DAYS.toFloat(),
                    steps = OnboardingViewModel.MAX_CYCLE_DAYS - OnboardingViewModel.MIN_CYCLE_DAYS - 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${OnboardingViewModel.MIN_CYCLE_DAYS} días",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${OnboardingViewModel.MAX_CYCLE_DAYS} días",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Regularidad percibida
            OnboardingSectionCard(
                title = "4. Regularidad de tus ciclos",
                subtitle = "Nos ayuda a estimar ventanas más amplias y realistas sin alertas innecesarias."
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CycleRegularity.entries.forEach { option ->
                        FilterChip(
                            selected = uiState.regularity == option,
                            onClick = { viewModel.onRegularitySelected(option) },
                            label = { Text(option.toDisplayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 5. Método anticonceptivo habitual
            OnboardingSectionCard(
                title = "5. Método anticonceptivo habitual",
                subtitle = "Los métodos hormonales o de barrera influyen de manera distinta en las proyecciones."
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContraceptiveMethod.entries.forEach { method ->
                        FilterChip(
                            selected = uiState.contraceptiveMethod == method,
                            onClick = { viewModel.onContraceptiveMethodSelected(method) },
                            label = { Text(method.toDisplayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Mensaje de validación suave si aplica
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
                onClick = { viewModel.completeOnboarding() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (uiState.isSaving) "Guardando tu perfil..." else "Comenzar seguimiento contextual",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

fun CycleRegularity.toDisplayName(): String = when (this) {
    CycleRegularity.REGULAR -> "Regular (varía pocos días)"
    CycleRegularity.IRREGULAR -> "Irregular (varía bastante)"
    CycleRegularity.UNKNOWN -> "No estoy segura"
}

fun ContraceptiveMethod.toDisplayName(): String = when (this) {
    ContraceptiveMethod.NONE -> "Ninguno"
    ContraceptiveMethod.BARRIER_CONDOM -> "Condón / Barrera"
    ContraceptiveMethod.COMBINED_ORAL_PILL -> "Pastilla oral combinada"
    ContraceptiveMethod.PROGESTIN_ONLY_PILL -> "Pastilla solo progestina"
    ContraceptiveMethod.HORMONAL_IUD -> "DIU hormonal"
    ContraceptiveMethod.COPPER_IUD -> "DIU de cobre"
    ContraceptiveMethod.SUBDERMAL_IMPLANT -> "Implante subdérmico"
    ContraceptiveMethod.INJECTION -> "Inyección anticonceptiva"
    ContraceptiveMethod.VAGINAL_RING_OR_PATCH -> "Anillo vaginal o Parche"
}
