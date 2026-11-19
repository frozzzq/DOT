package com.dot.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dot.app.domain.model.ArticleCategory
import com.dot.app.domain.model.CycleClassification
import com.dot.app.domain.model.EvaluatedCycle
import com.dot.app.domain.model.ForumCategory
import com.dot.app.presentation.theme.DotBlueDark
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDustyBlue
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseDark
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageGreen
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo orgánico pastel para encabezados de pantalla (Consejos, Foro, Ciclos).
 * Suaviza los headers con círculos difusos en la paleta oficial Dot.
 */
@Composable
fun DotSoftHeaderSurface(
    accentColor: Color = DotBlushPink,
    secondaryAccent: Color = DotSageSoft,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.62f),
                        accentColor.copy(alpha = 0.25f)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = DotWhite.copy(alpha = 0.42f),
                radius = size.height * 0.85f,
                center = Offset(size.width * 0.92f, size.height * 0.15f)
            )
            drawCircle(
                color = secondaryAccent.copy(alpha = 0.55f),
                radius = size.height * 0.58f,
                center = Offset(size.width * 0.10f, size.height * 0.95f)
            )
            drawCircle(
                color = DotRoseStrong.copy(alpha = 0.08f),
                radius = size.height * 0.36f,
                center = Offset(size.width * 0.72f, size.height * 0.85f)
            )
        }
        content()
    }
}

/**
 * Avatar suave en pastel para usuarias del foro o Guía Dot verificada.
 */
@Composable
fun DotSoftAvatar(
    alias: String,
    isAnonymous: Boolean = false,
    isVerifiedGuide: Boolean = false,
    size: Dp = 38.dp
) {
    val (bgColor, fgColor) = when {
        isVerifiedGuide -> DotSageSoft to DotSageDark
        isAnonymous -> DotBlushSurface to DotRoseStrong
        else -> {
            val palette = listOf(
                DotBlushPink to DotRoseDark,
                DotBlueSoft to DotBlueDark,
                DotSageSoft to DotSageDark,
                DotBlushSurface to DotDeepSlate
            )
            palette[abs(alias.hashCode()) % palette.size]
        }
    }

    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                isVerifiedGuide -> {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = "Guía verificada",
                        tint = fgColor,
                        modifier = Modifier.size(size * 0.54f)
                    )
                }

                isAnonymous || alias.equals("Anónima", ignoreCase = true) -> {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Anónima",
                        tint = fgColor,
                        modifier = Modifier.size(size * 0.52f)
                    )
                }

                else -> {
                    Text(
                        text = alias.trim().firstOrNull()?.uppercase() ?: "D",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fgColor,
                        fontSize = (size.value * 0.40f).sp
                    )
                }
            }
        }
    }
}

/**
 * Insignia circular suave con icono para encabezados de tarjetas y categorías.
 */
@Composable
fun DotSoftIconBadge(
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    size: Dp = 36.dp,
    iconSize: Dp = 19.dp
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Ilustraciones editoriales vectoriales en Compose Canvas para cada artículo de Consejos.
 * 100% offline, ultra ligeras y estrictamente en la paleta oficial de Dot.
 */
@Composable
fun ArticleCoverIllustration(
    articleId: String,
    category: ArticleCategory,
    modifier: Modifier = Modifier
) {
    val palette = articlePaletteFor(articleId, category)

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(palette.bgStart, palette.bgEnd)
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.52f
            val minDim = minOf(w, h)

            // Halo orgánico suave de fondo común para dar unidad editorial
            drawCircle(
                color = DotWhite.copy(alpha = 0.36f),
                radius = minDim * 0.58f,
                center = Offset(w * 0.16f, h * 0.18f)
            )
            drawCircle(
                color = palette.accentSoft.copy(alpha = 0.42f),
                radius = minDim * 0.45f,
                center = Offset(w * 0.88f, h * 0.88f)
            )

            when (articleId) {
                "fases-ciclo-menstrual" -> {
                    // Anillo de 4 fases menstruales (editorial estilo Flo)
                    val ringRadius = minDim * 0.33f
                    drawCircle(
                        color = DotWhite.copy(alpha = 0.72f),
                        radius = ringRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = minDim * 0.085f)
                    )
                    // Arco menstrual
                    drawArc(
                        color = DotRoseStrong,
                        startAngle = -90f,
                        sweepAngle = 72f,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = minDim * 0.085f, cap = StrokeCap.Round)
                    )
                    // Arco fértil en salvia
                    drawArc(
                        color = DotSageGreen,
                        startAngle = 55f,
                        sweepAngle = 68f,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = minDim * 0.085f, cap = StrokeCap.Round)
                    )
                    // Arco lúteo suave en azul pizarra
                    drawArc(
                        color = DotDustyBlue,
                        startAngle = 155f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - ringRadius, cy - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = minDim * 0.085f, cap = StrokeCap.Round)
                    )
                    // 4 puntos representando las 4 fases
                    val angles = listOf(-55.0, 20.0, 90.0, 200.0)
                    val dotColors = listOf(DotRoseStrong, DotBlushPink, DotSageGreen, DotDeepSlate)
                    angles.forEachIndexed { idx, deg ->
                        val rad = deg * PI / 180.0
                        val px = cx + (ringRadius * cos(rad)).toFloat()
                        val py = cy + (ringRadius * sin(rad)).toFloat()
                        drawCircle(color = DotWhite, radius = minDim * 0.068f, center = Offset(px, py))
                        drawCircle(color = dotColors[idx], radius = minDim * 0.044f, center = Offset(px, py))
                    }
                    // Centro luminoso
                    drawCircle(
                        color = DotWhite.copy(alpha = 0.85f),
                        radius = minDim * 0.14f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = DotRoseStrong.copy(alpha = 0.22f),
                        radius = minDim * 0.07f,
                        center = Offset(cx, cy)
                    )
                }

                "ciclo-normal-vs-irregular" -> {
                    // Barras equilibradas + banda saludable en salvia
                    val barCount = 5
                    val heights = listOf(0.54f, 0.62f, 0.58f, 0.64f, 0.56f)
                    val barWidth = minDim * 0.11f
                    val gap = minDim * 0.065f
                    val totalW = barCount * barWidth + (barCount - 1) * gap
                    val startX = cx - totalW / 2f
                    val baseY = cy + minDim * 0.28f

                    // Banda de rango normal
                    drawRoundRect(
                        color = DotWhite.copy(alpha = 0.65f),
                        topLeft = Offset(startX - minDim * 0.10f, baseY - minDim * 0.54f),
                        size = Size(totalW + minDim * 0.20f, minDim * 0.24f),
                        cornerRadius = CornerRadius(minDim * 0.08f)
                    )

                    heights.forEachIndexed { idx, factor ->
                        val bh = minDim * 0.72f * factor
                        val bx = startX + idx * (barWidth + gap)
                        drawRoundRect(
                            color = if (idx == 2) DotSageDark else DotSageGreen,
                            topLeft = Offset(bx, baseY - bh),
                            size = Size(barWidth, bh),
                            cornerRadius = CornerRadius(barWidth / 2f)
                        )
                    }
                }

                "postday-que-esperar" -> {
                    // Halo calmado + reloj suave de ventana de acción
                    drawCircle(
                        color = DotWhite.copy(alpha = 0.75f),
                        radius = minDim * 0.34f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = DotDustyBlue.copy(alpha = 0.55f),
                        radius = minDim * 0.34f,
                        center = Offset(cx, cy),
                        style = Stroke(width = minDim * 0.045f)
                    )
                    // Arco suave de tiempo
                    drawArc(
                        color = DotDeepSlate,
                        startAngle = -90f,
                        sweepAngle = 210f,
                        useCenter = false,
                        topLeft = Offset(cx - minDim * 0.24f, cy - minDim * 0.24f),
                        size = Size(minDim * 0.48f, minDim * 0.48f),
                        style = Stroke(width = minDim * 0.055f, cap = StrokeCap.Round)
                    )
                    // Manecillas minimalistas
                    drawLine(
                        color = DotDeepSlate,
                        start = Offset(cx, cy),
                        end = Offset(cx, cy - minDim * 0.13f),
                        strokeWidth = minDim * 0.042f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = DotRoseStrong,
                        start = Offset(cx, cy),
                        end = Offset(cx + minDim * 0.11f, cy + minDim * 0.04f),
                        strokeWidth = minDim * 0.042f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = DotRoseStrong, radius = minDim * 0.04f, center = Offset(cx, cy))
                }

                "olvido-pastilla-anticonceptiva" -> {
                    // Blíster minimalista pastel (tarjetita con circulitos ordenados)
                    val cardW = minDim * 0.86f
                    val cardH = minDim * 0.56f
                    drawRoundRect(
                        color = DotWhite.copy(alpha = 0.88f),
                        topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(minDim * 0.14f)
                    )
                    val cols = 4
                    val rows = 2
                    val cellW = cardW / (cols + 1)
                    val cellH = cardH / (rows + 1)
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val px = (cx - cardW / 2f) + cellW * (c + 1)
                            val py = (cy - cardH / 2f) + cellH * (r + 1)
                            val isTodayPill = (r == 0 && c == 2)
                            drawCircle(
                                color = when {
                                    isTodayPill -> DotRoseStrong
                                    r == 0 && c < 2 -> DotSageGreen
                                    else -> DotBlushPink
                                },
                                radius = if (isTodayPill) minDim * 0.072f else minDim * 0.056f,
                                center = Offset(px, py)
                            )
                            if (isTodayPill) {
                                drawCircle(
                                    color = DotWhite,
                                    radius = minDim * 0.025f,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }

                "alivio-colicos-menstruales" -> {
                    // Flor orgánica de calma + ondas cálidas
                    val petalR = minDim * 0.16f
                    val orbitR = minDim * 0.17f
                    for (i in 0 until 6) {
                        val angle = (i * 60.0) * PI / 180.0
                        val px = cx + (orbitR * cos(angle)).toFloat()
                        val py = cy + (orbitR * sin(angle)).toFloat()
                        drawCircle(
                            color = if (i % 2 == 0) DotWhite.copy(alpha = 0.85f) else DotBlushPink.copy(alpha = 0.85f),
                            radius = petalR,
                            center = Offset(px, py)
                        )
                    }
                    drawCircle(
                        color = DotRoseStrong.copy(alpha = 0.85f),
                        radius = minDim * 0.13f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = DotWhite.copy(alpha = 0.65f),
                        radius = minDim * 0.055f,
                        center = Offset(cx - minDim * 0.03f, cy - minDim * 0.03f)
                    )
                }

                else -> {
                    // Círculos entrelazados suaves (protección, bienestar y cuidado)
                    drawCircle(
                        color = DotWhite.copy(alpha = 0.82f),
                        radius = minDim * 0.26f,
                        center = Offset(cx - minDim * 0.14f, cy)
                    )
                    drawCircle(
                        color = DotRoseStrong.copy(alpha = 0.34f),
                        radius = minDim * 0.26f,
                        center = Offset(cx + minDim * 0.14f, cy)
                    )
                    drawCircle(
                        color = DotSageGreen.copy(alpha = 0.55f),
                        radius = minDim * 0.13f,
                        center = Offset(cx, cy)
                    )
                    // Pequeño destello superior
                    val starPath = Path().apply {
                        val sx = cx + minDim * 0.32f
                        val sy = cy - minDim * 0.24f
                        val sr = minDim * 0.07f
                        moveTo(sx, sy - sr)
                        quadraticTo(sx, sy, sx + sr, sy)
                        quadraticTo(sx, sy, sx, sy + sr)
                        quadraticTo(sx, sy, sx - sr, sy)
                        quadraticTo(sx, sy, sx, sy - sr)
                        close()
                    }
                    drawPath(starPath, color = DotWhite.copy(alpha = 0.9f))
                }
            }
        }
    }
}

private data class ArticleVisualPalette(
    val bgStart: Color,
    val bgEnd: Color,
    val accentSoft: Color
)

private fun articlePaletteFor(articleId: String, category: ArticleCategory): ArticleVisualPalette =
    when (articleId) {
        "fases-ciclo-menstrual" -> ArticleVisualPalette(DotBlushPink, DotBlushSurface, DotRoseStrong)
        "ciclo-normal-vs-irregular" -> ArticleVisualPalette(DotSageSoft, DotSageGreen.copy(alpha = 0.45f), DotSageDark)
        "postday-que-esperar" -> ArticleVisualPalette(DotBlueSoft, DotDustyBlue.copy(alpha = 0.55f), DotDeepSlate)
        "olvido-pastilla-anticonceptiva" -> ArticleVisualPalette(DotSageSoft, DotBlushSurface, DotSageGreen)
        "alivio-colicos-menstruales" -> ArticleVisualPalette(DotBlushSurface, DotBlushPink, DotRoseStrong)
        else -> when (category) {
            ArticleCategory.CYCLE -> ArticleVisualPalette(DotBlushPink, DotBlushSurface, DotRoseStrong)
            ArticleCategory.CONTRACEPTION -> ArticleVisualPalette(DotBlueSoft, DotBlushSurface, DotDustyBlue)
            ArticleCategory.EMERGENCY -> ArticleVisualPalette(DotBlueSoft, DotDustyBlue.copy(alpha = 0.48f), DotDeepSlate)
            ArticleCategory.SYMPTOMS -> ArticleVisualPalette(DotBlushSurface, DotBlushPink, DotRoseStrong)
            ArticleCategory.WELLNESS -> ArticleVisualPalette(DotSageSoft, DotSageGreen.copy(alpha = 0.4f), DotSageDark)
        }
    }

/**
 * Icono representativo para cada categoría de Consejos.
 */
fun ArticleCategory.toIcon(): ImageVector = when (this) {
    ArticleCategory.CYCLE -> Icons.Outlined.WaterDrop
    ArticleCategory.CONTRACEPTION -> Icons.Outlined.Medication
    ArticleCategory.EMERGENCY -> Icons.Outlined.HealthAndSafety
    ArticleCategory.SYMPTOMS -> Icons.Outlined.Spa
    ArticleCategory.WELLNESS -> Icons.Outlined.FavoriteBorder
}

/**
 * Icono representativo para cada categoría del Foro.
 */
fun ForumCategory.toIcon(): ImageVector = when (this) {
    ForumCategory.CYCLE -> Icons.Outlined.WaterDrop
    ForumCategory.CONTRACEPTION -> Icons.Outlined.Medication
    ForumCategory.SYMPTOMS -> Icons.Outlined.Spa
    ForumCategory.EMERGENCY -> Icons.Outlined.HealthAndSafety
    ForumCategory.WELLNESS -> Icons.Outlined.FavoriteBorder
}

/**
 * Colores suaves (fondo, texto) para cada categoría del Foro.
 */
fun ForumCategory.toSoftColors(): Pair<Color, Color> = when (this) {
    ForumCategory.CYCLE -> DotBlushPink.copy(alpha = 0.65f) to DotRoseDark
    ForumCategory.CONTRACEPTION -> DotSageSoft to DotSageDark
    ForumCategory.SYMPTOMS -> DotBlushSurface to DotRoseStrong
    ForumCategory.EMERGENCY -> DotBlueSoft to DotBlueDark
    ForumCategory.WELLNESS -> DotSageSoft to DotDeepSlate
}

/**
 * Anillo visual de progreso del ciclo menstrual alrededor del círculo hero en Hoy.
 * Dibuja los días de menstruación en #B77076, la ventana fértil en #B0BB91 y un punto luminoso en el día actual.
 */
@Composable
fun CycleProgressRingOverlay(
    currentDay: Int,
    expectedCycleLength: Int,
    expectedBleedingDays: Int = 5,
    isCurrentlyMenstruating: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val inset = strokeWidth / 2f + 4.dp.toPx()
        val diameter = size.minDimension - inset * 2
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val total = expectedCycleLength.coerceAtLeast(21).toFloat()

        // 1. Pista circular suave
        drawArc(
            color = DotBlushSurface,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 2. Arco de fase menstrual (primeros días del ciclo)
        val bleedingSweep = ((expectedBleedingDays.coerceIn(3, 8) / total) * 360f).coerceIn(28f, 110f)
        drawArc(
            color = DotRoseStrong.copy(alpha = 0.72f),
            startAngle = -90f,
            sweepAngle = bleedingSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 3. Arco de ventana fértil estimada (aprox. día total - 17 al día total - 12)
        val fertileStartDay = (total - 17f).coerceAtLeast(expectedBleedingDays + 2f)
        val fertileStartAngle = -90f + (fertileStartDay / total) * 360f
        val fertileSweep = (5f / total) * 360f
        drawArc(
            color = DotSageGreen.copy(alpha = 0.85f),
            startAngle = fertileStartAngle,
            sweepAngle = fertileSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 4. Punto indicador del día actual sobre el anillo
        val clampedDay = currentDay.coerceIn(1, total.toInt())
        val currentAngleDeg = -90.0 + ((clampedDay - 0.5) / total.toDouble()) * 360.0
        val currentRad = currentAngleDeg * PI / 180.0
        val radius = diameter / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val dotX = cx + (radius * cos(currentRad)).toFloat()
        val dotY = cy + (radius * sin(currentRad)).toFloat()

        drawCircle(
            color = DotWhite,
            radius = 9.dp.toPx(),
            center = Offset(dotX, dotY)
        )
        drawCircle(
            color = if (isCurrentlyMenstruating) DotRoseStrong else DotDeepSlate,
            radius = 5.5.dp.toPx(),
            center = Offset(dotX, dotY)
        )
    }
}

/**
 * Gráfica visual minimalista de duración de los últimos ciclos para la pantalla Ciclos.
 * Muestra la franja clínica normal (24–38 días) en salvia suave y cada ciclo como una barra redondeada.
 */
@Composable
fun CycleLengthMiniChartCard(
    cycles: List<EvaluatedCycle>,
    modifier: Modifier = Modifier
) {
    val chartItems = cycles.take(6).reversed()
    if (chartItems.isEmpty()) return

    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("es", "MX"))
    val maxDays = maxOf(42, chartItems.maxOf { it.effectiveLengthDays } + 4).toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ritmo de tus ciclos",
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotSageSoft
                ) {
                    Text(
                        text = "Normal 24–38 d",
                        style = MaterialTheme.typography.labelSmall,
                        color = DotSageDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
            ) {
                // Franja de referencia saludable 24–38 días
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp, bottom = 22.dp)
                ) {
                    val topNormalY = size.height * (1f - (38f / maxDays).coerceIn(0f, 1f))
                    val bottomNormalY = size.height * (1f - (24f / maxDays).coerceIn(0f, 1f))
                    drawRoundRect(
                        color = DotSageSoft.copy(alpha = 0.7f),
                        topLeft = Offset(0f, topNormalY),
                        size = Size(size.width, (bottomNormalY - topNormalY).coerceAtLeast(8f)),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                }

                // Barras por ciclo
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartItems.forEach { item ->
                        val barColor = when (item.classification) {
                            CycleClassification.NORMAL_REGULAR -> DotSageGreen
                            CycleClassification.IRREGULAR -> DotDustyBlue
                            CycleClassification.ANOMALOUS -> DotRoseStrong
                            CycleClassification.ALTERED_BY_EMERGENCY -> DotDeepSlate
                            CycleClassification.IN_PROGRESS -> DotBlushPink
                        }
                        val fraction = (item.effectiveLengthDays / maxDays).coerceIn(0.18f, 1f)
                        val barHeight = (78f * fraction).dp

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${item.effectiveLengthDays}d",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DotDeepSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = barHeight)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = item.cycle.startDate.format(monthFormatter)
                                    .replace(".", "")
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = DotMutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
