package com.dot.app.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Remove
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.CycleRegularity
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotOutlineSoft
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class OnboardingDatePickerTarget {
    NONE,
    START_DATE,
    END_DATE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activePicker by remember { mutableStateOf(OnboardingDatePickerTarget.NONE) }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "MX"))
    }
    val shortFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingFinished()
        }
    }

    if (activePicker != OnboardingDatePickerTarget.NONE) {
        val baseDate = if (activePicker == OnboardingDatePickerTarget.START_DATE) {
            uiState.lastPeriodStartDate
        } else {
            uiState.lastPeriodEndDate
        }
        val initialMillis = baseDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { activePicker = OnboardingDatePickerTarget.NONE },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            if (activePicker == OnboardingDatePickerTarget.START_DATE) {
                                viewModel.onLastPeriodStartDateChanged(selected)
                            } else {
                                viewModel.onLastPeriodEndDateChanged(selected)
                            }
                        }
                        activePicker = OnboardingDatePickerTarget.NONE
                    }
                ) {
                    Text("Confirmar", color = DotRoseStrong)
                }
            },
            dismissButton = {
                TextButton(onClick = { activePicker = OnboardingDatePickerTarget.NONE }) {
                    Text("Cancelar", color = DotDeepSlate)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DotBlushPink.copy(alpha = 0.7f), DotBlushCanvas, DotWhite)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Encabezado con retroceso y progreso limpio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentStep > 0) {
                    IconButton(onClick = viewModel::previousStep) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Paso anterior",
                            tint = DotDeepSlate
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                LinearProgressIndicator(
                    progress = { (uiState.currentStep + 1f) / uiState.totalSteps.toFloat() },
                    color = DotRoseStrong,
                    trackColor = DotBlushPink,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                )

                Text(
                    text = "${uiState.currentStep + 1}/${uiState.totalSteps}",
                    style = MaterialTheme.typography.labelLarge,
                    color = DotDeepSlate
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido del paso actual (1 sola pregunta clara por pantalla)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    label = "onboarding_step"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        when (step) {
                            0 -> StepPeriodStart(
                                startDate = uiState.lastPeriodStartDate,
                                dateFormatter = dateFormatter,
                                onSelectPreset = viewModel::onLastPeriodStartDateChanged,
                                onOpenPicker = { activePicker = OnboardingDatePickerTarget.START_DATE }
                            )

                            1 -> StepPeriodEndOrOngoing(
                                startDate = uiState.lastPeriodStartDate,
                                endDate = uiState.lastPeriodEndDate,
                                isStillMenstruating = uiState.isStillMenstruating,
                                bleedingDays = uiState.averagePeriodDuration,
                                shortFormatter = shortFormatter,
                                onStillMenstruatingChanged = viewModel::onStillMenstruatingChanged,
                                onOpenEndPicker = { activePicker = OnboardingDatePickerTarget.END_DATE },
                                onDurationStep = viewModel::onBleedingDurationStepperChanged
                            )

                            2 -> StepRegularityAndLength(
                                regularity = uiState.regularity,
                                cycleLength = uiState.averageCycleLength,
                                onRegularitySelected = viewModel::onRegularitySelected,
                                onCycleLengthChanged = viewModel::onCycleLengthChanged
                            )

                            3 -> StepContraception(
                                selected = uiState.contraceptiveMethod,
                                onSelected = viewModel::onContraceptiveMethodSelected
                            )

                            4 -> StepReminders(
                                enablePillReminder = uiState.enableDailyPillReminder,
                                pillHour = uiState.pillReminderHour,
                                pillMinute = uiState.pillReminderMinute,
                                enablePeriodReminder = uiState.enablePeriodReminder,
                                onTogglePill = viewModel::onEnableDailyPillReminderChanged,
                                onPillTimeChanged = viewModel::onPillReminderTimeChanged,
                                onTogglePeriod = viewModel::onEnablePeriodReminderChanged
                            )
                        }
                    }
                }
            }

            uiState.validationMessage?.let { msg ->
                Surface(
                    color = DotBlushPink,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotInkBlack,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = viewModel::nextStep,
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DotRoseStrong,
                    contentColor = DotWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = when {
                        uiState.isSaving -> "Calculando tu ciclo..."
                        uiState.currentStep == uiState.totalSteps - 1 -> "Comenzar en Dot"
                        else -> "Continuar"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = DotWhite
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepPeriodStart(
    startDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onSelectPreset: (LocalDate) -> Unit,
    onOpenPicker: () -> Unit
) {
    Text(
        text = "¿Cuándo empezó tu último periodo?",
        style = MaterialTheme.typography.headlineLarge,
        color = DotInkBlack
    )
    Text(
        text = "Tu ciclo comienza el primer día de sangrado (Día 1) y termina cuando inicia una nueva menstruación.",
        style = MaterialTheme.typography.bodyLarge,
        color = DotDeepSlate
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "PRIMER DÍA DE MENSTRUACIÓN",
                style = MaterialTheme.typography.labelMedium,
                color = DotMutedText
            )
            Text(
                text = startDate.format(dateFormatter),
                style = MaterialTheme.typography.headlineMedium,
                color = DotRoseStrong,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onOpenPicker,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, DotRoseStrong)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = DotRoseStrong,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Elegir en el calendario", color = DotRoseStrong)
            }
        }
    }

    Text(
        text = "Accesos rápidos",
        style = MaterialTheme.typography.labelLarge,
        color = DotDeepSlate
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "Hoy" to LocalDate.now(),
            "Ayer" to LocalDate.now().minusDays(1),
            "Hace 3 días" to LocalDate.now().minusDays(3),
            "Hace 1 semana" to LocalDate.now().minusDays(7),
            "Hace 2 semanas" to LocalDate.now().minusDays(14)
        ).forEach { (label, date) ->
            FilterChip(
                selected = startDate == date,
                onClick = { onSelectPreset(date) },
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
}

@Composable
private fun StepPeriodEndOrOngoing(
    startDate: LocalDate,
    endDate: LocalDate,
    isStillMenstruating: Boolean,
    bleedingDays: Int,
    shortFormatter: DateTimeFormatter,
    onStillMenstruatingChanged: (Boolean) -> Unit,
    onOpenEndPicker: () -> Unit,
    onDurationStep: (Int) -> Unit
) {
    Text(
        text = "¿Todavía te está bajando o ya terminó?",
        style = MaterialTheme.typography.headlineLarge,
        color = DotInkBlack
    )
    Text(
        text = "Así marcamos exactamente tus días de menstruación en tu calendario y calculamos tu ciclo.",
        style = MaterialTheme.typography.bodyLarge,
        color = DotDeepSlate
    )

    SelectableOptionCard(
        title = "Todavía me está bajando",
        subtitle = "Registraremos desde el ${startDate.format(shortFormatter)} hasta hoy como días activos.",
        selected = isStillMenstruating,
        onClick = { onStillMenstruatingChanged(true) }
    )

    SelectableOptionCard(
        title = "Ya terminó mi periodo",
        subtitle = "Indica el último día de sangrado para guardarlo en tu calendario.",
        selected = !isStillMenstruating,
        onClick = { onStillMenstruatingChanged(false) }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isStillMenstruating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Último día de sangrado",
                            style = MaterialTheme.typography.labelMedium,
                            color = DotMutedText
                        )
                        Text(
                            text = "${startDate.format(shortFormatter)} — ${endDate.format(shortFormatter)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = DotRoseStrong
                        )
                    }
                    TextButton(onClick = onOpenEndPicker) {
                        Text("Cambiar", color = DotRoseStrong)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isStillMenstruating) {
                            "¿Cuántos días suele durarte?"
                        } else {
                            "Duración de este periodo"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = "Lo habitual es entre 3 y 7 días",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotMutedText
                    )
                }
                NumberStepper(
                    value = bleedingDays,
                    suffix = "días",
                    onDecrement = { onDurationStep(bleedingDays - 1) },
                    onIncrement = { onDurationStep(bleedingDays + 1) }
                )
            }
        }
    }
}

@Composable
private fun StepRegularityAndLength(
    regularity: CycleRegularity,
    cycleLength: Int,
    onRegularitySelected: (CycleRegularity) -> Unit,
    onCycleLengthChanged: (Int) -> Unit
) {
    Text(
        text = "¿Cómo suelen ser tus ciclos?",
        style = MaterialTheme.typography.headlineLarge,
        color = DotInkBlack
    )
    Text(
        text = "No existe un único ciclo perfecto. Conocer tu patrón evita alertas innecesarias.",
        style = MaterialTheme.typography.bodyLarge,
        color = DotDeepSlate
    )

    SelectableOptionCard(
        title = "Regular",
        subtitle = "Suele llegar en fechas similares (varía pocos días).",
        selected = regularity == CycleRegularity.REGULAR,
        onClick = { onRegularitySelected(CycleRegularity.REGULAR) }
    )

    SelectableOptionCard(
        title = "Irregular",
        subtitle = "Cambia bastante de un mes a otro o se adelanta/retrasa.",
        selected = regularity == CycleRegularity.IRREGULAR,
        onClick = { onRegularitySelected(CycleRegularity.IRREGULAR) }
    )

    SelectableOptionCard(
        title = "No estoy segura",
        subtitle = "Dot irá aprendiendo de tus registros mes con mes.",
        selected = regularity == CycleRegularity.UNKNOWN,
        onClick = { onRegularitySelected(CycleRegularity.UNKNOWN) }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Duración promedio del ciclo",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Text(
                    text = "Rango clínico habitual: 24 a 38 días",
                    style = MaterialTheme.typography.labelMedium,
                    color = DotMutedText
                )
            }
            NumberStepper(
                value = cycleLength,
                suffix = "días",
                onDecrement = { onCycleLengthChanged(cycleLength - 1) },
                onIncrement = { onCycleLengthChanged(cycleLength + 1) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepContraception(
    selected: ContraceptiveMethod,
    onSelected: (ContraceptiveMethod) -> Unit
) {
    Text(
        text = "¿Usas algún método anticonceptivo?",
        style = MaterialTheme.typography.headlineLarge,
        color = DotInkBlack
    )
    Text(
        text = "Nos ayuda a adaptar tus proyecciones y sugerirte recordatorios útiles si tomas pastillas.",
        style = MaterialTheme.typography.bodyLarge,
        color = DotDeepSlate
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ContraceptiveMethod.entries.forEach { method ->
            val isSelected = selected == method
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) DotRoseStrong else DotWhite,
                border = BorderStroke(1.dp, if (isSelected) DotRoseStrong else DotOutlineSoft),
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelected(method) }
            ) {
                Text(
                    text = method.toDisplayName(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) DotWhite else DotInkBlack,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun StepReminders(
    enablePillReminder: Boolean,
    pillHour: Int,
    pillMinute: Int,
    enablePeriodReminder: Boolean,
    onTogglePill: (Boolean) -> Unit,
    onPillTimeChanged: (Int, Int) -> Unit,
    onTogglePeriod: (Boolean) -> Unit
) {
    Text(
        text = "Recordatorios para cuidarte",
        style = MaterialTheme.typography.headlineLarge,
        color = DotInkBlack
    )
    Text(
        text = "Activa solo los que necesites. Podrás cambiarlos o activar el Modo Discreto cuando quieras.",
        style = MaterialTheme.typography.bodyLarge,
        color = DotDeepSlate
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = DotBlushPink,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Medication,
                            contentDescription = null,
                            tint = DotRoseStrong
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pastilla anticonceptiva diaria",
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = "Aviso diario para no olvidar tu toma",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotMutedText
                    )
                }
                Switch(
                    checked = enablePillReminder,
                    onCheckedChange = onTogglePill,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DotWhite,
                        checkedTrackColor = DotRoseStrong
                    )
                )
            }

            if (enablePillReminder) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DotBlushSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hora diaria",
                        style = MaterialTheme.typography.labelLarge,
                        color = DotDeepSlate
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onPillTimeChanged((pillHour + 23) % 24, pillMinute) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Restar hora", tint = DotDeepSlate)
                        }
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", pillHour, pillMinute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DotRoseStrong
                        )
                        IconButton(
                            onClick = { onPillTimeChanged((pillHour + 1) % 24, pillMinute) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Sumar hora", tint = DotDeepSlate)
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = DotBlushPink,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = DotRoseStrong
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avisos de tu periodo",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Text(
                    text = "Te recordamos antes de tu ventana menstrual y para registrar tu regla.",
                    style = MaterialTheme.typography.labelMedium,
                    color = DotMutedText
                )
            }
            Switch(
                checked = enablePeriodReminder,
                onCheckedChange = onTogglePeriod,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DotWhite,
                    checkedTrackColor = DotRoseStrong
                )
            )
        }
    }
}

@Composable
private fun SelectableOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) DotBlushPink.copy(alpha = 0.45f) else DotWhite,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) DotRoseStrong else DotOutlineSoft
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DotMutedText
                )
            }
            Surface(
                shape = CircleShape,
                color = if (selected) DotRoseStrong else DotBlushSurface,
                modifier = Modifier.size(28.dp)
            ) {
                if (selected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = DotWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberStepper(
    value: Int,
    suffix: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = DotBlushSurface,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(onClick = onDecrement)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Remove, contentDescription = "Disminuir", tint = DotDeepSlate, modifier = Modifier.size(18.dp))
            }
        }
        Text(
            text = "$value $suffix",
            style = MaterialTheme.typography.titleMedium,
            color = DotRoseStrong,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = CircleShape,
            color = DotBlushSurface,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(onClick = onIncrement)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, contentDescription = "Aumentar", tint = DotDeepSlate, modifier = Modifier.size(18.dp))
            }
        }
    }
}

fun CycleRegularity.toDisplayName(): String = when (this) {
    CycleRegularity.REGULAR -> "Regular"
    CycleRegularity.IRREGULAR -> "Irregular"
    CycleRegularity.UNKNOWN -> "No estoy segura"
}

fun ContraceptiveMethod.toDisplayName(): String = when (this) {
    ContraceptiveMethod.NONE -> "Ninguno"
    ContraceptiveMethod.BARRIER_CONDOM -> "Condón"
    ContraceptiveMethod.COMBINED_ORAL_PILL -> "Pastilla diaria combinada"
    ContraceptiveMethod.PROGESTIN_ONLY_PILL -> "Pastilla solo progestina"
    ContraceptiveMethod.HORMONAL_IUD -> "DIU hormonal"
    ContraceptiveMethod.COPPER_IUD -> "DIU de cobre"
    ContraceptiveMethod.SUBDERMAL_IMPLANT -> "Implante subdérmico"
    ContraceptiveMethod.INJECTION -> "Inyección"
    ContraceptiveMethod.VAGINAL_RING_OR_PATCH -> "Anillo o Parche"
}
