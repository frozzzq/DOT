package com.dot.app.presentation.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.usecase.CalendarHighlights
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDividerSoft
import com.dot.app.presentation.theme.DotDustyBlue
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageGreen
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Calendario en grande deslizable verticalmente con touch (de arriba a abajo).
 * Resalta en rosita fuerte (#B77076) todos los días de menstruación guardados
 * en el cuestionario inicial y registros posteriores.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleCalendarScreen(
    onNavigateBack: () -> Unit,
    onEditDate: (LocalDate) -> Unit,
    viewModel: CycleCalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = uiState.currentMonthIndex)

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "MX"))
    }
    val selectedDateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "MX"))
    }

    LaunchedEffect(Unit) {
        if (uiState.currentMonthIndex in uiState.months.indices) {
            listState.scrollToItem(uiState.currentMonthIndex)
        }
    }

    Scaffold(
        containerColor = DotBlushCanvas,
        topBar = {
            Column(modifier = Modifier.background(DotWhite)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Calendario",
                            style = MaterialTheme.typography.titleLarge,
                            color = DotInkBlack
                        )
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DotWhite)
                )

                // Leyenda compacta y clara
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = DotRoseStrong, label = "Menstruación")
                    LegendDot(color = DotBlushPink, label = "Estimado", borderColor = DotRoseStrong)
                    LegendDot(color = DotSageGreen, label = "Fértil")
                    LegendDot(color = DotDeepSlate, label = "Con notas")
                }

                // Encabezado fijo de días de la semana (L M M J V S D)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    listOf("L", "M", "M", "J", "V", "S", "D").forEach { dayLetter ->
                        Text(
                            text = dayLetter,
                            style = MaterialTheme.typography.labelMedium,
                            color = DotMutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(color = DotDividerSoft)
            }
        },
        bottomBar = {
            // Barra inferior interactiva del día seleccionado
            Surface(
                color = DotWhite,
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = uiState.selectedDate.format(selectedDateFormatter)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            color = DotInkBlack
                        )
                        val statusLabel = when {
                            uiState.selectedDate in uiState.highlights.menstruationDays ->
                                "Día de menstruación registrado"
                            uiState.selectedDate in uiState.highlights.predictedPeriodDays ->
                                "Periodo estimado"
                            uiState.selectedDate in uiState.highlights.fertileDays ->
                                "Ventana fértil estimada"
                            uiState.selectedDayLog != null ->
                                "Con síntomas o notas registradas"
                            else -> "Sin registros para este día"
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DotMutedText
                        )
                    }

                    if (!uiState.selectedDate.isAfter(uiState.today)) {
                        Button(
                            onClick = { onEditDate(uiState.selectedDate) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DotRoseStrong,
                                contentColor = DotWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Registrar", color = DotWhite)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Calendario vertical deslizable por meses (touch scroll arriba y abajo)
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState.months,
                key = { it.toString() }
            ) { yearMonth ->
                MonthCalendarCard(
                    yearMonth = yearMonth,
                    monthTitle = yearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                    today = uiState.today,
                    selectedDate = uiState.selectedDate,
                    highlights = uiState.highlights,
                    onSelectDay = viewModel::onDaySelected
                )
            }
        }
    }
}

@Composable
private fun MonthCalendarCard(
    yearMonth: YearMonth,
    monthTitle: String,
    today: LocalDate,
    selectedDate: LocalDate,
    highlights: CalendarHighlights,
    onSelectDay: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    // Lunes = 1 -> offset 0; Domingo = 7 -> offset 6
    val leadingEmptySlots = firstDayOfMonth.dayOfWeek.value - 1
    val totalDays = yearMonth.lengthOfMonth()
    val totalCells = leadingEmptySlots + totalDays
    val rows = (totalCells + 6) / 7

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = DotWhite,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                color = DotInkBlack,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingEmptySlots + 1
                        if (dayNumber in 1..totalDays) {
                            val date = yearMonth.atDay(dayNumber)
                            CalendarDayCell(
                                date = date,
                                isToday = date == today,
                                isSelected = date == selectedDate,
                                isMenstruation = date in highlights.menstruationDays,
                                isPredictedPeriod = date in highlights.predictedPeriodDays,
                                isFertile = date in highlights.fertileDays,
                                hasLog = date in highlights.loggedDays,
                                hasEmergencyPill = date in highlights.emergencyPillDays,
                                onClick = { onSelectDay(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isMenstruation: Boolean,
    isPredictedPeriod: Boolean,
    isFertile: Boolean,
    hasLog: Boolean,
    hasEmergencyPill: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tonalidad rosita fuerte #B77076 para los días de menstruación
    val backgroundColor = when {
        isMenstruation -> DotRoseStrong
        isPredictedPeriod -> DotBlushPink.copy(alpha = 0.7f)
        isFertile -> DotSageSoft
        else -> Color.Transparent
    }

    val textColor = when {
        isMenstruation -> DotWhite
        else -> DotInkBlack
    }

    val borderStroke = when {
        isSelected -> BorderStroke(2.dp, DotDeepSlate)
        isToday -> BorderStroke(1.5.dp, DotRoseStrong)
        isPredictedPeriod -> BorderStroke(1.dp, DotRoseStrong.copy(alpha = 0.7f))
        else -> null
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isMenstruation || isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasLog || hasEmergencyPill) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isMenstruation -> DotWhite
                                hasEmergencyPill -> DotDustyBlue
                                else -> DotDeepSlate
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String,
    borderColor: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape) else Modifier
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DotDeepSlate
        )
    }
}
