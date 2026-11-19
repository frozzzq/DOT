package com.dot.app.presentation.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ArticleCategory
import com.dot.app.domain.model.HealthArticle
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
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
            containerColor = DotWhite
        ) {
            ArticleReaderContent(
                article = article,
                onClose = viewModel::onArticleClosed
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DotBlushCanvas)
    ) {
        // Encabezado limpio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DotBlushPink.copy(alpha = 0.45f))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Consejos",
                style = MaterialTheme.typography.headlineMedium,
                color = DotInkBlack
            )
            Text(
                text = "Información real y verificada sobre tu salud menstrual.",
                style = MaterialTheme.typography.bodyMedium,
                color = DotDeepSlate
            )
        }

        // Filtros horizontales limpios
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { viewModel.onCategorySelected(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DotRoseStrong,
                    selectedLabelColor = DotWhite,
                    containerColor = DotWhite,
                    labelColor = DotInkBlack
                )
            )
            ArticleCategory.entries.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DotRoseStrong,
                        selectedLabelColor = DotWhite,
                        containerColor = DotWhite,
                        labelColor = DotInkBlack
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState.filteredArticles,
                key = { it.id }
            ) { article ->
                ArticleCard(
                    article = article,
                    onClick = { viewModel.onArticleOpened(article) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ArticleCard(
    article: HealthArticle,
    onClick: () -> Unit
) {
    val badgeBg = when (article.category) {
        ArticleCategory.CYCLE -> DotBlushPink
        ArticleCategory.CONTRACEPTION -> DotSageSoft
        ArticleCategory.EMERGENCY -> DotBlueSoft
        ArticleCategory.SYMPTOMS -> DotBlushSurface
        ArticleCategory.WELLNESS -> DotSageSoft
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBg
                ) {
                    Text(
                        text = article.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = DotInkBlack,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = DotMutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${article.readTimeMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = DotMutedText
                    )
                }
            }

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                color = DotInkBlack
            )

            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = DotMutedText
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    tint = DotSageDark,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = article.medicalSource,
                    style = MaterialTheme.typography.labelSmall,
                    color = DotDeepSlate
                )
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
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = DotBlushPink
            ) {
                Text(
                    text = article.category.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DotInkBlack,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = DotDeepSlate)
            }
        }

        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            color = DotInkBlack
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DotSageSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Idea clave",
                    style = MaterialTheme.typography.labelLarge,
                    color = DotSageDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.keyTakeaway,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DotInkBlack
                )
            }
        }

        article.sections.forEach { section ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = section.heading,
                    style = MaterialTheme.typography.titleMedium,
                    color = DotRoseStrong
                )
                Text(
                    text = section.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DotInkBlack
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DotBlueSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    tint = DotDeepSlate,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Fuente: ${article.medicalSource}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DotDeepSlate
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
