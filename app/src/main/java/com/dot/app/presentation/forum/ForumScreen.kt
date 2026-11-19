package com.dot.app.presentation.forum

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.ForumCategory
import com.dot.app.domain.model.ForumPost
import com.dot.app.domain.model.ForumReply
import com.dot.app.presentation.components.DotSoftAvatar
import com.dot.app.presentation.components.DotSoftHeaderSurface
import com.dot.app.presentation.components.toIcon
import com.dot.app.presentation.components.toSoftColors
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotDividerSoft
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotOutlineSoft
import com.dot.app.presentation.theme.DotRoseDark
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageDark
import com.dot.app.presentation.theme.DotSageGreen
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
            // Encabezado suave con fondo orgánico pastel
            DotSoftHeaderSurface(
                accentColor = DotBlushPink,
                secondaryAccent = DotBlueSoft
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
                            text = "Comunidad",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DotInkBlack
                        )
                        Text(
                            text = "Espacio seguro, respetuoso y anónimo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DotDeepSlate
                        )
                    }

                    // Mini pila visual de avatares pastel
                    Box(
                        modifier = Modifier.width(68.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(modifier = Modifier.offset(x = (-32).dp)) {
                            DotSoftAvatar(alias = "Sofía", size = 30.dp)
                        }
                        Box(modifier = Modifier.offset(x = (-16).dp)) {
                            DotSoftAvatar(alias = "Anónima", isAnonymous = true, size = 30.dp)
                        }
                        DotSoftAvatar(alias = "Guía", isVerifiedGuide = true, size = 30.dp)
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
                ForumCategoryPill(
                    label = "Todas",
                    icon = Icons.Outlined.GridView,
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) }
                )
                ForumCategory.entries.forEach { cat ->
                    ForumCategoryPill(
                        label = cat.label,
                        icon = cat.toIcon(),
                        selected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Tarjeta ligera para invitar a preguntar sin saturar texto
                item(key = "quick_ask_banner") {
                    QuickAskPromptCard(
                        onClick = { viewModel.onShowCreateDialog(true) }
                    )
                }

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
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Preguntar", style = MaterialTheme.typography.labelLarge, color = DotWhite)
            }
        }
    }
}

@Composable
private fun ForumCategoryPill(
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
private fun QuickAskPromptCard(
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = DotWhite,
        border = BorderStroke(1.dp, DotBlushPink),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DotSoftAvatar(alias = "Anónima", isAnonymous = true, size = 36.dp)
            Text(
                text = "¿Tienes alguna duda? Pregunta en confianza...",
                style = MaterialTheme.typography.bodyMedium,
                color = DotMutedText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = DotBlushSurface
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Nueva pregunta",
                    tint = DotRoseStrong,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(18.dp)
                )
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
    val (catBg, catFg) = post.category.toSoftColors()
    val hasVerifiedGuide = post.replies.any { it.isEvidenceBasedTip }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila de autora con avatar pastel + categoría con icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    DotSoftAvatar(
                        alias = post.authorAlias,
                        isAnonymous = post.isAnonymous,
                        size = 36.dp
                    )
                    Column {
                        Text(
                            text = post.authorAlias,
                            style = MaterialTheme.typography.labelLarge,
                            color = DotInkBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = DotMutedText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = catBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = post.category.toIcon(),
                            contentDescription = null,
                            tint = catFg,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = post.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = catFg
                        )
                    }
                }
            }

            // Pregunta principal limpia y escaneable
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                lineHeight = 22.sp,
                color = DotInkBlack
            )

            // Vista previa breve (máx. 2 líneas) para evitar saturación visual
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = DotMutedText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Indicador visual suave si incluye respuesta verificada de Guía Dot
            if (hasVerifiedGuide) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DotSageSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = DotSageDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Con respuesta verificada",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DotSageDark
                        )
                    }
                }
            }

            // Pie limpio con pastillas interactivas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (post.isMarkedHelpfulByMe) DotBlushPink.copy(alpha = 0.7f) else DotBlushCanvas,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onToggleHelpful)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (post.isMarkedHelpfulByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Me ayudó",
                                tint = if (post.isMarkedHelpfulByMe) DotRoseStrong else DotMutedText,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${post.helpfulCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (post.isMarkedHelpfulByMe) DotRoseDark else DotDeepSlate
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = DotBlushCanvas
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Respuestas",
                                tint = DotMutedText,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${post.replies.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = DotDeepSlate
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Ver hilo",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotRoseStrong,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = DotRoseStrong,
                        modifier = Modifier.size(18.dp)
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
    val (catBg, catFg) = post.category.toSoftColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera de autora + botón cerrar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                DotSoftAvatar(
                    alias = post.authorAlias,
                    isAnonymous = post.isAnonymous,
                    size = 40.dp
                )
                Column {
                    Text(
                        text = post.authorAlias,
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = post.createdDate.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = DotMutedText
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = DotBlushCanvas,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = DotDeepSlate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Pregunta en tarjeta suave
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DotBlushCanvas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = catBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = post.category.toIcon(),
                            contentDescription = null,
                            tint = catFg,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = post.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = catFg
                        )
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

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (post.isMarkedHelpfulByMe) DotBlushPink else DotWhite,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleHelpful)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Respuestas (${post.replies.size})",
                style = MaterialTheme.typography.titleMedium,
                color = DotInkBlack
            )
        }

        // Respuestas en burbujas conversacionales suaves
        post.replies.forEach { reply ->
            ForumReplyBubble(
                reply = reply,
                dateLabel = reply.createdDate.format(dateFormatter)
            )
        }

        // Aviso breve y discreto de una sola línea
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DotBlueSoft.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = DotDeepSlate,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Apoyo comunitario • No sustituye una valoración médica.",
                style = MaterialTheme.typography.labelSmall,
                color = DotDeepSlate
            )
        }

        HorizontalDivider(color = DotDividerSoft)

        // Barra compacta de respuesta + pastilla de anonimato
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (replyAnonymous) DotBlushSurface else DotWhite,
                    border = BorderStroke(1.dp, if (replyAnonymous) DotRoseStrong.copy(alpha = 0.4f) else DotDividerSoft),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { replyAnonymous = !replyAnonymous }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (replyAnonymous) Icons.Outlined.VisibilityOff else Icons.Outlined.Person,
                            contentDescription = null,
                            tint = if (replyAnonymous) DotRoseStrong else DotDeepSlate,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (replyAnonymous) "Respondiendo como anónima" else "Respondiendo con tu nombre",
                            style = MaterialTheme.typography.labelMedium,
                            color = DotDeepSlate
                        )
                    }
                }
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
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DotRoseStrong,
                        unfocusedBorderColor = DotOutlineSoft,
                        focusedContainerColor = DotBlushCanvas,
                        unfocusedContainerColor = DotBlushCanvas
                    ),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = if (replyText.isNotBlank()) DotRoseStrong else DotBlushSurface,
                    modifier = Modifier.size(46.dp)
                ) {
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
                            tint = if (replyText.isNotBlank()) DotWhite else DotRoseStrong,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
    }
}

@Composable
private fun ForumReplyBubble(
    reply: ForumReply,
    dateLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        DotSoftAvatar(
            alias = reply.authorAlias,
            isAnonymous = reply.authorAlias.equals("Anónima", ignoreCase = true),
            isVerifiedGuide = reply.isEvidenceBasedTip,
            size = 34.dp
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = if (reply.isEvidenceBasedTip) DotSageSoft else DotBlushCanvas,
            border = if (reply.isEvidenceBasedTip) {
                BorderStroke(1.dp, DotSageGreen.copy(alpha = 0.6f))
            } else {
                BorderStroke(1.dp, DotDividerSoft.copy(alpha = 0.7f))
            },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (reply.isEvidenceBasedTip) "Guía Dot" else reply.authorAlias,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (reply.isEvidenceBasedTip) DotSageDark else DotInkBlack,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (reply.isEvidenceBasedTip) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = DotWhite.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = "Evidencia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DotSageDark,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = DotMutedText
                    )
                }

                Text(
                    text = reply.body,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = DotInkBlack
                )
            }
        }
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
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = DotBlushPink,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = DotRoseStrong,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text("Nueva pregunta", style = MaterialTheme.typography.titleLarge, color = DotInkBlack)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ForumCategory.entries.forEach { item ->
                        ForumCategoryPill(
                            label = item.label,
                            icon = item.toIcon(),
                            selected = category == item,
                            onClick = { category = item }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tu pregunta") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Cuéntanos un poco más") },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DotBlushCanvas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = DotRoseStrong,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Publicar de forma anónima",
                                style = MaterialTheme.typography.labelLarge,
                                color = DotDeepSlate
                            )
                        }
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublish(title, body, category, isAnonymous) },
                enabled = title.isNotBlank() && body.isNotBlank(),
                shape = RoundedCornerShape(50),
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
