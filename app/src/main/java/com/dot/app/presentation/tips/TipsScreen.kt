package com.dot.app.presentation.tips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ArticleCategory
import com.dot.app.domain.model.ArticleSection
import com.dot.app.domain.model.HealthArticle
import com.dot.app.presentation.components.ArticleCoverIllustration
import com.dot.app.presentation.components.DotSoftHeaderSurface
import com.dot.app.presentation.components.DotSoftIconBadge
import com.dot.app.presentation.components.toIcon
import com.dot.app.presentation.theme.DotBlueDark
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDividerSoft
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseDark
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageGreen
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    viewModel: TipsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.selectedArticle?.let { article ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onArticleClosed,
            sheetState = sheetState,
            containerColor = DotWhite,
            dragHandle = null
        ) {
            ArticleReaderContent(
                article = article,
                onClose = viewModel::onArticleClosed
            )
        }
    }

    val filtered = uiState.filteredArticles
    val showFeaturedHero = uiState.selectedCategory == null && filtered.size > 1
    val featuredArticle = if (showFeaturedHero) filtered.first() else null
    val gridArticles = if (showFeaturedHero) filtered.drop(1) else filtered
    val articleRows = gridArticles.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DotBlushCanvas)
    ) {
        // Encabezado suave con fondo orgánico pastel
        DotSoftHeaderSurface(
            accentColor = DotBlushPink,
            secondaryAccent = DotSageSoft
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Consejos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = "Guías claras y suaves para conocer tu cuerpo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotDeepSlate
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotWhite.copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = DotSageDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "OMS / ACOG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DotDeepSlate
                        )
                    }
                }
            }
        }

        // Filtros visuales suaves con iconos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftCategoryPill(
                label = "Todos",
                icon = Icons.Outlined.GridView,
                selected = uiState.selectedCategory == null,
                onClick = { viewModel.onCategorySelected(null) }
            )
            ArticleCategory.entries.forEach { category ->
                SoftCategoryPill(
                    label = category.label,
                    icon = category.toIcon(),
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Tarjeta destacada editorial estilo revista visual
            featuredArticle?.let { hero ->
                item(key = "hero_${hero.id}") {
                    FeaturedArticleHeroCard(
                        article = hero,
                        onClick = { viewModel.onArticleOpened(hero) }
                    )
                }

                item(key = "section_explore") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guías para ti",
                            style = MaterialTheme.typography.titleMedium,
                            color = DotInkBlack
                        )
                        Text(
                            text = "${gridArticles.size} lecturas breves",
                            style = MaterialTheme.typography.labelMedium,
                            color = DotMutedText
                        )
                    }
                }
            }

            // 2. Cuadrícula visual de 2 columnas (limpia, sin paredes de texto)
            items(
                items = articleRows,
                key = { row -> row.joinToString("_") { it.id } }
            ) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { article ->
                        VisualArticleGridTile(
                            article = article,
                            onClick = { viewModel.onArticleOpened(article) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun SoftCategoryPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) DotRoseStrong else DotWhite,
        border = if (selected) null else BorderStroke(1.dp, DotDividerSoft),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) DotWhite else DotRoseStrong,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) DotWhite else DotDeepSlate
            )
        }
    }
}

@Composable
private fun FeaturedArticleHeroCard(
    article: HealthArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                ArticleCoverIllustration(
                    articleId = article.id,
                    category = article.category,
                    modifier = Modifier.fillMaxSize()
                )

                // Etiqueta destacada flotante
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotRoseStrong,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = DotWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Destacado",
                            style = MaterialTheme.typography.labelSmall,
                            color = DotWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Tiempo de lectura flotante
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotWhite.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = DotDeepSlate,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${article.readTimeMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = DotDeepSlate,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = article.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DotRoseStrong,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = DotInkBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = DotBlushSurface,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Leer guía",
                            tint = DotRoseStrong,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualArticleGridTile(
    article: HealthArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (tagBg, tagFg) = article.category.toSoftBadgeColors()

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Portada ilustrada pastel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(106.dp)
            ) {
                ArticleCoverIllustration(
                    articleId = article.id,
                    category = article.category,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotWhite.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${article.readTimeMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = DotDeepSlate,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Bloque limpio con mínimo texto: categoría + título + fuente corta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = tagBg
                ) {
                    Text(
                        text = article.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tagFg,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = DotInkBlack,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(58.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = DotSageDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = shortSourceBadge(article.medicalSource),
                        style = MaterialTheme.typography.labelSmall,
                        color = DotMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleReaderContent(
    article: HealthArticle,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Portada ilustrada del artículo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(166.dp)
        ) {
            ArticleCoverIllustration(
                articleId = article.id,
                category = article.category,
                modifier = Modifier.fillMaxSize()
            )

            // Pastilla de categoría y tiempo
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotWhite.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = article.category.toIcon(),
                            contentDescription = null,
                            tint = DotRoseStrong,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = article.category.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = DotInkBlack
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotWhite.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "${article.readTimeMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Botón cerrar flotante suave
            Surface(
                shape = CircleShape,
                color = DotWhite.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(36.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = DotDeepSlate,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DotInkBlack
                )
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DotMutedText
                )
            }

            // Tarjeta visual de Idea clave
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DotSageSoft,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    DotSoftIconBadge(
                        icon = Icons.Outlined.AutoAwesome,
                        backgroundColor = DotWhite.copy(alpha = 0.85f),
                        iconTint = DotSageDark,
                        size = 36.dp,
                        iconSize = 18.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lo más importante",
                            style = MaterialTheme.typography.labelLarge,
                            color = DotSageDark
                        )
                        Text(
                            text = article.keyTakeaway,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DotInkBlack
                        )
                    }
                }
            }

            // Tarjetas numeradas y aireadas por sección
            article.sections.forEachIndexed { index, section ->
                ArticleSectionSoftCard(
                    stepNumber = index + 1,
                    section = section
                )
            }

            // Fuente médica verificada
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DotBlueSoft,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = DotBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Fuente verificada: ${article.medicalSource}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun ArticleSectionSoftCard(
    stepNumber: Int,
    section: ArticleSection
) {
    val stepBg = when (stepNumber % 3) {
        1 -> DotBlushPink
        2 -> DotSageSoft
        else -> DotBlueSoft
    }
    val stepFg = when (stepNumber % 3) {
        1 -> DotRoseDark
        2 -> DotSageDark
        else -> DotBlueDark
    }

    // Limpia prefijos numéricos repetidos ("1. ") para que la insignia circular se vea limpia
    val cleanHeading = section.heading.replaceFirst(Regex("^\\d+\\.\\s*"), "")
    val lines = section.body.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DotBlushCanvas,
        border = BorderStroke(1.dp, DotDividerSoft.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = stepBg,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stepNumber.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = stepFg
                        )
                    }
                }
                Text(
                    text = cleanHeading,
                    style = MaterialTheme.typography.titleMedium,
                    color = DotInkBlack,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lines.forEach { line ->
                    if (line.startsWith("•")) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(DotRoseStrong.copy(alpha = 0.75f))
                            )
                            Text(
                                text = line.removePrefix("•").trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 21.sp,
                                color = DotDeepSlate,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = DotDeepSlate
                        )
                    }
                }
            }
        }
    }
}

private fun ArticleCategory.toSoftBadgeColors(): Pair<Color, Color> = when (this) {
    ArticleCategory.CYCLE -> DotBlushPink.copy(alpha = 0.72f) to DotRoseDark
    ArticleCategory.CONTRACEPTION -> DotSageSoft to DotSageDark
    ArticleCategory.EMERGENCY -> DotBlueSoft to DotBlueDark
    ArticleCategory.SYMPTOMS -> DotBlushSurface to DotRoseStrong
    ArticleCategory.WELLNESS -> DotSageSoft to DotSageDark
}

private fun shortSourceBadge(source: String): String {
    val prefix = source.substringBefore("—").trim()
    return if (prefix.length <= 18) prefix else prefix.take(16).trim() + "…"
}
