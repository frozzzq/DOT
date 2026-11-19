package com.dot.app.presentation.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ContraceptiveMethod
import com.dot.app.domain.model.CyclePhase
import com.dot.app.domain.model.CyclePrediction
import com.dot.app.domain.model.DailyLog
import com.dot.app.domain.model.EmpatheticMessage
import com.dot.app.domain.model.MessageCategory
import com.dot.app.domain.model.ProtectionMethod
import com.dot.app.presentation.daily_log.toDisplayName
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDustyBlue
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotOutlineSoft
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageGreen
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    onOpenCalendar: () -> Unit,
    onOpenNotifications: () -> Unit,
    onNavigateToDailyLog: (LocalDate?) -> Unit,
    onNavigateToEmergencyContraception: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val shortDateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))
    }
    val headerDateFormatter = remember {
        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX"))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DotBlushPink.copy(alpha = 0.72f),
                        DotBlushCanvas,
                        DotBlushCanvas
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Barra superior limpia: Saludo + Notificaciones y Calendario arriba a la derecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (uiState.userName.isNotBlank()) "Hola, ${uiState.userName}" else "Hoy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DotInkBlack
                )
                Text(
                    text = uiState.evaluationDate.format(headerDateFormatter),
                    style = MaterialTheme.typography.labelLarge,
                    color = DotDeepSlate
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de módulo de notificaciones y recordatorios
                Surface(
                    shape = CircleShape,
                    color = DotWhite,
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones y recordatorios",
                            tint = DotDeepSlate
                        )
                    }
                }

                // Botón del calendario arriba a la derecha
                Surface(
                    shape = CircleShape,
                    color = DotRoseStrong,
                    shadowElevation = 3.dp
                ) {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Abrir calendario completo",
                            tint = DotWhite
                        )
                    }
                }
            }
        }

        // 1. HERO CENTRAL ESTILO FLO: Día del ciclo EN GRANDE
        uiState.cyclePrediction?.let { prediction ->
            CycleHeroCircleCard(
                prediction = prediction,
                shortDateFormatter = shortDateFormatter,
                onTogglePeriodToday = viewModel::togglePeriodToday
            )
        }

        // 2. BOTÓN INTUITIVO PRINCIPAL PARA REGISTRAR SÍNTOMAS, SEXO, PASTILLAS Y POSTDAY
        SymptomQuickCtaCard(
            todayLog = uiState.todayLog,
            onLogToday = { onNavigateToDailyLog(LocalDate.now()) },
            onOpenEmergency = onNavigateToEmergencyContraception
        )

        // 3. Recordatorio rápido de pastilla anticonceptiva diaria (si aplica)
        val usesDailyPill = uiState.userProfile?.contraceptiveMethod == ContraceptiveMethod.COMBINED_ORAL_PILL ||
            uiState.userProfile?.contraceptiveMethod == ContraceptiveMethod.PROGESTIN_ONLY_PILL
        if (usesDailyPill) {
            DailyPillQuickCheckCard(
                takenToday = uiState.todayLog?.tookContraceptivePill == true,
                onToggleTaken = viewModel::toggleDailyPillTakenToday
            )
        }

        // 4. Resumen compacto de lo registrado hoy (si hay datos)
        uiState.todayLog?.let { log ->
            val hasInfo = log.symptoms.isNotEmpty() || log.hadSexualActivity ||
                log.tookContraceptivePill || log.tookEmergencyPill || log.hasMenstrualBleeding
            if (hasInfo) {
                TodaySummaryChipsCard(
                    log = log,
                    onEdit = { onNavigateToDailyLog(LocalDate.now()) }
                )
            }
        }

        // 5. Tarjeta de acompañamiento contextual y empático (RF-05, sin saturar)
        uiState.empatheticMessage?.let { message ->
            CompactEmpatheticCard(message = message)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CycleHeroCircleCard(
    prediction: CyclePrediction,
    shortDateFormatter: DateTimeFormatter,
    onTogglePeriodToday: () -> Unit
) {
    val phaseBadgeText = when {
        prediction.activeEmergencyContraception != null -> "Recalibrado por Postday"
        prediction.currentPhase == CyclePhase.MENSTRUAL -> "Fase menstrual"
        prediction.currentPhase == CyclePhase.FOLLICULAR -> "Fase folicular"
        prediction.currentPhase == CyclePhase.OVULATION_FERTILE -> "Ventana fértil estimada"
        else -> "Fase lútea"
    }

    val phasePillColor = when {
        prediction.activeEmergencyContraception != null -> DotDustyBlue
        prediction.currentPhase == CyclePhase.MENSTRUAL -> DotRoseStrong
        prediction.currentPhase == CyclePhase.OVULATION_FERTILE -> DotSageGreen
        else -> DotDeepSlate
    }

    val phasePillTextColor = when {
        prediction.activeEmergencyContraception != null -> DotInkBlack
        prediction.currentPhase == CyclePhase.OVULATION_FERTILE -> DotInkBlack
        else -> DotWhite
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = phasePillColor
            ) {
                Text(
                    text = phaseBadgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = phasePillTextColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Círculo rosado central con el DÍA DEL CICLO en grande
            Box(
                modifier = Modifier
                    .size(204.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (prediction.isCurrentlyMenstruating) DotRoseStrong else DotBlushPink,
                                if (prediction.isCurrentlyMenstruating) DotRoseStrong.copy(alpha = 0.85f) else DotBlushSurface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val heroTextColor = if (prediction.isCurrentlyMenstruating) DotWhite else DotInkBlack
                val heroSubColor = if (prediction.isCurrentlyMenstruating) {
                    DotWhite.copy(alpha = 0.9f)
                } else {
                    DotDeepSlate
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (prediction.isCurrentlyMenstruating) "Periodo activo" else "Tu ciclo",
                        style = MaterialTheme.typography.labelLarge,
                        color = heroSubColor
                    )
                    Text(
                        text = "Día ${prediction.currentDayOfCycle}",
                        fontSize = 44.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = heroTextColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "de ~${prediction.expectedCycleLengthDays} días",
                        style = MaterialTheme.typography.bodyMedium,
                        color = heroSubColor
                    )
                }
            }

            // Ventana estimada clara y concisa
            Text(
                text = if (prediction.isCurrentlyMenstruating) {
                    "Inicio de tu regla: ${prediction.referenceCycleStartDate.format(shortDateFormatter)}"
                } else {
                    "Próximo periodo estimado: ${prediction.estimatedWindowStart.format(shortDateFormatter)} – ${prediction.estimatedWindowEnd.format(shortDateFormatter)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DotDeepSlate,
                textAlign = TextAlign.Center
            )

            // Botón limpio de 1 toque para iniciar o finalizar periodo
            Surface(
                shape = RoundedCornerShape(50),
                color = if (prediction.isCurrentlyMenstruating) DotBlushSurface else DotRoseStrong,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onTogglePeriodToday)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = if (prediction.isCurrentlyMenstruating) DotRoseStrong else DotWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (prediction.isCurrentlyMenstruating) {
                            "Terminó mi periodo"
                        } else {
                            "Registrar que me bajó"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (prediction.isCurrentlyMenstruating) DotInkBlack else DotWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SymptomQuickCtaCard(
    todayLog: DailyLog?,
    onLogToday: () -> Unit,
    onOpenEmergency: () -> Unit
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¿Cómo te sientes hoy?",
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = "Registra cólicos, dolor de cabeza, sexo, pastillas o flujo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotMutedText
                    )
                }
            }

            Button(
                onClick = onLogToday,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DotRoseStrong,
                    contentColor = DotWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = DotWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (todayLog != null) "Editar síntomas de hoy" else "Registrar síntomas y día",
                    style = MaterialTheme.typography.labelLarge,
                    color = DotWhite
                )
            }

            // Acceso discreto y directo a Postday (RF-03)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DotBlueSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenEmergency)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HealthAndSafety,
                        contentDescription = null,
                        tint = DotDeepSlate,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "¿Tomaste pastilla del día siguiente (Postday)? Regístrala aquí",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyPillQuickCheckCard(
    takenToday: Boolean,
    onToggleTaken: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (takenToday) DotSageSoft else DotWhite,
        border = BorderStroke(1.dp, if (takenToday) DotSageGreen else DotOutlineSoft),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggleTaken)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (takenToday) Icons.Outlined.CheckCircle else Icons.Outlined.Medication,
                contentDescription = null,
                tint = if (takenToday) DotSageDark else DotRoseStrong,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (takenToday) "Pastilla anticonceptiva tomada" else "Pastilla anticonceptiva de hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Text(
                    text = if (takenToday) "Excelente, registrada en tu día de hoy." else "Toca aquí para marcar tu toma diaria.",
                    style = MaterialTheme.typography.labelMedium,
                    color = DotMutedText
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodaySummaryChipsCard(
    log: DailyLog,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registrado hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Text(
                    text = "Editar",
                    style = MaterialTheme.typography.labelLarge,
                    color = DotRoseStrong
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (log.hasMenstrualBleeding) {
                    SummaryTag(text = log.flowLevel.toDisplayName(), bg = DotBlushPink, fg = DotInkBlack)
                }
                if (log.hadSexualActivity) {
                    val sexLabel = when (log.protectionUsed) {
                        ProtectionMethod.BARRIER_CONDOM -> "Sexo con condón"
                        ProtectionMethod.NONE -> "Sexo sin condón"
                        else -> "Actividad sexual"
                    }
                    SummaryTag(text = sexLabel, bg = DotBlueSoft, fg = DotDeepSlate)
                }
                if (log.tookContraceptivePill) {
                    SummaryTag(text = "Pastilla anticonceptiva", bg = DotSageSoft, fg = DotSageDark)
                }
                if (log.tookEmergencyPill) {
                    SummaryTag(text = "Postday", bg = DotDustyBlue, fg = DotInkBlack)
                }
                log.symptoms.forEach { symptom ->
                    SummaryTag(text = symptom.toDisplayName(), bg = DotBlushSurface, fg = DotInkBlack)
                }
            }
        }
    }
}

@Composable
private fun SummaryTag(text: String, bg: Color, fg: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CompactEmpatheticCard(message: EmpatheticMessage) {
    val bgColor = when (message.category) {
        MessageCategory.CYCLE_IN_PROGRESS_NORMAL,
        MessageCategory.WITHIN_EXPECTED_WINDOW -> DotSageSoft
        MessageCategory.EXPLAINABLE_VARIATION_EMERGENCY_PILL,
        MessageCategory.EXPLAINABLE_VARIATION_IRREGULARITY -> DotBlueSoft
        MessageCategory.MEDICAL_CHECKUP_SUGGESTION -> DotBlushPink.copy(alpha = 0.65f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = DotDeepSlate,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
            }
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = DotDeepSlate
            )
            message.actionableGuidance?.let { guidance ->
                Text(
                    text = guidance,
                    style = MaterialTheme.typography.labelMedium,
                    color = DotInkBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
