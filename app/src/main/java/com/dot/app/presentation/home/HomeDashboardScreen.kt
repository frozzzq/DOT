package com.dot.app.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.model.FlowLevel
import com.dot.app.domain.model.MessageCategory
import com.dot.app.domain.model.PredictionConfidenceContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigateToDailyLog: (LocalDate?) -> Unit,
    onNavigateToEmergencyContraception: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val shortDateFormatter = remember {
        DateTimeFormatter.ofPattern("d 'de' MMM", Locale("es", "MX"))
    }
    val fullDateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "MX"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dot",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.evaluationDate.format(fullDateFormatter)
                                .replaceFirstChar { it.uppercase() },
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Estado actual del ciclo (RF-04)
            uiState.cyclePrediction?.let { prediction ->
                CycleStatusSummaryCard(
                    prediction = prediction,
                    shortDateFormatter = shortDateFormatter
                )
            }

            // 2. Tarjeta principal con mensaje empático adaptado al contexto (RF-05, RNF-03)
            uiState.empatheticMessage?.let { message ->
                EmpatheticContextualCard(
                    message = message,
                    shortDateFormatter = shortDateFormatter
                )
            }

            // 3. Accesos directos rápidos (RNF-03: máximo 3 interacciones)
            Text(
                text = "Acciones de seguimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            QuickActionsSection(
                todayLog = uiState.todayLog,
                onNavigateToDailyLog = { onNavigateToDailyLog(LocalDate.now()) },
                onNavigateToEmergencyContraception = onNavigateToEmergencyContraception
            )

            // 4. Registros recientes con opción de edición rápida (RF-08)
            if (uiState.recentLogs.isNotEmpty()) {
                RecentLogsCard(
                    recentLogs = uiState.recentLogs.take(5),
                    shortDateFormatter = shortDateFormatter,
                    onSelectDateToEdit = { date -> onNavigateToDailyLog(date) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CycleStatusSummaryCard(
    prediction: CyclePrediction,
    shortDateFormatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = prediction.confidenceContext.toFriendlyBadge(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = "Día ${prediction.currentDayOfCycle} del ciclo",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Inicio del ciclo actual: ${prediction.referenceCycleStartDate.format(shortDateFormatter)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ventana estimada del próximo periodo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${prediction.estimatedWindowStart.format(shortDateFormatter)} — ${prediction.estimatedWindowEnd.format(shortDateFormatter)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpatheticContextualCard(
    message: EmpatheticMessage,
    shortDateFormatter: DateTimeFormatter
) {
    val containerColor = when (message.category) {
        MessageCategory.CYCLE_IN_PROGRESS_NORMAL,
        MessageCategory.WITHIN_EXPECTED_WINDOW -> MaterialTheme.colorScheme.secondaryContainer
        MessageCategory.EXPLAINABLE_VARIATION_EMERGENCY_PILL,
        MessageCategory.EXPLAINABLE_VARIATION_IRREGULARITY -> MaterialTheme.colorScheme.tertiaryContainer
        MessageCategory.MEDICAL_CHECKUP_SUGGESTION -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (message.category) {
        MessageCategory.CYCLE_IN_PROGRESS_NORMAL,
        MessageCategory.WITHIN_EXPECTED_WINDOW -> MaterialTheme.colorScheme.onSecondaryContainer
        MessageCategory.EXPLAINABLE_VARIATION_EMERGENCY_PILL,
        MessageCategory.EXPLAINABLE_VARIATION_IRREGULARITY -> MaterialTheme.colorScheme.onTertiaryContainer
        MessageCategory.MEDICAL_CHECKUP_SUGGESTION -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = contentColor
                )
                Text(
                    text = "Acompañamiento contextual",
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }

            Text(
                text = message.title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )

            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = message.reassuranceNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    message.actionableGuidance?.let { guidance ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = guidance,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    message.suggestedPregnancyTestDate?.let { testDate ->
                        Text(
                            text = "Fecha orientativa para prueba confiable: a partir del ${testDate.format(shortDateFormatter)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    todayLog: DailyLog?,
    onNavigateToDailyLog: () -> Unit,
    onNavigateToEmergencyContraception: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Acceso directo a Registro Diario (RF-02, RF-08)
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registro diario y calendario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (todayLog != null) {
                                "Hoy registraste: flujo ${todayLog.flowLevel.toShortLabel().lowercase()}, ${todayLog.symptoms.size} síntoma(s)."
                            } else {
                                "Registra flujo menstrual, síntomas físicos o actividad sexual de hoy o días pasados."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onNavigateToDailyLog,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (todayLog != null) "Ver o editar registro diario" else "Registrar el día de hoy"
                    )
                }
            }
        }

        // Acceso directo a Anticoncepción de Emergencia (RF-03, RF-06)
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
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anticoncepción de emergencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Si tomaste una pastilla del día siguiente, regístrala aquí para recalibrar tu ciclo sin falsas alarmas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onNavigateToEmergencyContraception,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrar pastilla de emergencia")
                }
            }
        }
    }
}

@Composable
private fun RecentLogsCard(
    recentLogs: List<DailyLog>,
    shortDateFormatter: DateTimeFormatter,
    onSelectDateToEdit: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Registros recientes (toca para editar)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            recentLogs.forEach { log ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDateToEdit(log.date) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = log.date.format(shortDateFormatter),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Flujo: ${log.flowLevel.toShortLabel()} • Síntomas: ${log.symptoms.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun PredictionConfidenceContext.toFriendlyBadge(): String = when (this) {
    PredictionConfidenceContext.STANDARD_REGULAR -> "Ciclo regular • Ventana flexible"
    PredictionConfidenceContext.EXPANDED_IRREGULAR -> "Rango amplio adaptado a tu ciclo"
    PredictionConfidenceContext.RECALCULATED_EMERGENCY_CONTRACEPTION -> "Ventana recalibrada por emergencia"
    PredictionConfidenceContext.HORMONAL_CONTRACEPTION_CONTROLLED -> "Seguimiento con método hormonal"
}

private fun FlowLevel.toShortLabel(): String = when (this) {
    FlowLevel.NONE -> "Sin flujo"
    FlowLevel.SPOTTING -> "Manchado"
    FlowLevel.LIGHT -> "Ligero"
    FlowLevel.MODERATE -> "Moderado"
    FlowLevel.HEAVY -> "Abundante"
}
