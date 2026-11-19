package com.dot.app.presentation.forum

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ForumPost
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDividerSoft
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: ForumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX")) }

    if (uiState.showCreateDialog) {
        CreateForumPostDialog(
            onDismiss = { viewModel.onShowCreateDialog(false) },
            onPublish = { title, body, category, anonymous ->
                viewModel.createPost(title, body, category, anonymous)
            }
        )
    }

    uiState.activePost?.let { post ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onClosePost,
            sheetState = sheetState,
            containerColor = DotWhite
        ) {
            ForumThreadDetailSheet(
                post = post,
                dateFormatter = dateFormatter,
                onClose = viewModel::onClosePost,
                onToggleHelpful = { viewModel.toggleHelpful(post.id) },
                onSendReply = { replyText, anonymous ->
                    viewModel.addReply(post.id, replyText, anonymous)
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DotBlushCanvas)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Encabezado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DotBlushPink.copy(alpha = 0.45f))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Foro de comunidad",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DotInkBlack
                )
                Text(
                    text = "Pregunta en confianza, comparte experiencias y lee respuestas útiles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DotDeepSlate
                )
            }

            // Categorías
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
                    label = { Text("Todas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DotRoseStrong,
                        selectedLabelColor = DotWhite,
                        containerColor = DotWhite,
                        labelColor = DotInkBlack
                    )
                )
                ForumCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(cat.label) },
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uiState.filteredPosts,
                    key = { it.id }
                ) { post ->
                    ForumPostCard(
                        post = post,
                        dateLabel = post.createdDate.format(dateFormatter),
                        onClick = { viewModel.onOpenPost(post.id) },
                        onToggleHelpful = { viewModel.toggleHelpful(post.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onShowCreateDialog(true) },
            containerColor = DotRoseStrong,
            contentColor = DotWhite,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Preguntar", style = MaterialTheme.typography.labelLarge, color = DotWhite)
            }
        }
    }
}

@Composable
private fun ForumPostCard(
    post: ForumPost,
    dateLabel: String,
    onClick: () -> Unit,
    onToggleHelpful: () -> Unit
) {
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
                    color = DotBlushSurface
                ) {
                    Text(
                        text = post.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = DotRoseStrong,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "${post.authorAlias} • $dateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = DotMutedText
                )
            }

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = DotInkBlack
            )

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = DotMutedText,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleHelpful)
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (post.isMarkedHelpfulByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Me ayudó",
                        tint = if (post.isMarkedHelpfulByMe) DotRoseStrong else DotMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.helpfulCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Respuestas",
                        tint = DotMutedText,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "${post.replies.size} respuestas",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate
                    )
                }
            }
        }
    }
}

@Composable
private fun ForumThreadDetailSheet(
    post: ForumPost,
    dateFormatter: DateTimeFormatter,
    onClose: () -> Unit,
    onToggleHelpful: () -> Unit,
    onSendReply: (String, Boolean) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var replyAnonymous by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${post.authorAlias} • ${post.createdDate.format(dateFormatter)}",
                style = MaterialTheme.typography.labelMedium,
                color = DotMutedText
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = DotDeepSlate)
            }
        }

        Text(
            text = post.title,
            style = MaterialTheme.typography.titleLarge,
            color = DotInkBlack
        )
        Text(
            text = post.body,
            style = MaterialTheme.typography.bodyLarge,
            color = DotDeepSlate
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = DotBlushSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleHelpful)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (post.isMarkedHelpfulByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = DotRoseStrong,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Me ayudó (${post.helpfulCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotInkBlack
                    )
                }
            }
        }

        HorizontalDivider(color = DotDividerSoft)

        Text(
            text = "Respuestas (${post.replies.size})",
            style = MaterialTheme.typography.titleMedium,
            color = DotInkBlack
        )

        post.replies.forEach { reply ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (reply.isEvidenceBasedTip) DotSageSoft else DotBlushCanvas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (reply.isEvidenceBasedTip) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = DotSageDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = reply.authorAlias,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (reply.isEvidenceBasedTip) DotSageDark else DotInkBlack,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = reply.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotInkBlack
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DotBlueSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "El foro es un espacio de apoyo comunitario y no sustituye una valoración médica.",
                style = MaterialTheme.typography.labelSmall,
                color = DotDeepSlate,
                modifier = Modifier.padding(10.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Responder como anónima",
                style = MaterialTheme.typography.labelMedium,
                color = DotDeepSlate
            )
            Switch(
                checked = replyAnonymous,
                onCheckedChange = { replyAnonymous = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DotWhite,
                    checkedTrackColor = DotRoseStrong
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Escribe una respuesta respetuosa...") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (replyText.isNotBlank()) {
                        onSendReply(replyText, replyAnonymous)
                        replyText = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar respuesta",
                    tint = DotRoseStrong
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
private fun CreateForumPostDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, ForumCategory, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ForumCategory.CYCLE) }
    var isAnonymous by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DotWhite,
        title = {
            Text("Nueva pregunta", style = MaterialTheme.typography.titleLarge, color = DotInkBlack)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ForumCategory.entries.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DotRoseStrong,
                                selectedLabelColor = DotWhite
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tu pregunta") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Cuéntanos un poco más") },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publicar de forma anónima", style = MaterialTheme.typography.bodyMedium, color = DotDeepSlate)
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DotWhite,
                            checkedTrackColor = DotRoseStrong
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublish(title, body, category, isAnonymous) },
                enabled = title.isNotBlank() && body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DotRoseStrong, contentColor = DotWhite)
            ) {
                Text("Publicar", color = DotWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = DotDeepSlate)
            }
        }
    )
}
