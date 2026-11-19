package com.dot.app.presentation.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.domain.model.NotificationReminder
import com.dot.app.domain.model.ReminderType
import com.dot.app.presentation.theme.DotBlueSoft
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotSageSoft
import com.dot.app.presentation.theme.DotWhite
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.sendTestNotification()
        }
    }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) {
            onLoggedOut()
        }
    }

    Scaffold(
        containerColor = DotBlushCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notificaciones y cuenta",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Modo discreto de privacidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DotBlueSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DotDeepSlate,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = DotWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo discreto",
                            style = MaterialTheme.typography.titleMedium,
                            color = DotInkBlack
                        )
                        Text(
                            text = "Oculta palabras como 'periodo' o 'pastilla' en la pantalla bloqueada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DotDeepSlate
                        )
                    }
                    Switch(
                        checked = uiState.discreetMode,
                        onCheckedChange = viewModel::onToggleDiscreetMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DotWhite,
                            checkedTrackColor = DotDeepSlate
                        )
                    )
                }
            }

            // Lista de recordatorios configurables
            uiState.reminders.forEach { reminder ->
                ReminderConfigCard(
                    reminder = reminder,
                    onToggle = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.onToggleReminder(reminder, enabled)
                    },
                    onAdjustHour = { delta -> viewModel.onUpdateReminderHour(reminder, delta) },
                    onAdjustMinute = { delta -> viewModel.onUpdateReminderMinute(reminder, delta) }
                )
            }

            uiState.feedbackMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DotSageSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotInkBlack,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Botón para probar notificación real al instante
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.sendTestNotification()
                    }
                },
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
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = DotWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Probar notificación ahora", color = DotWhite)
            }

            // Tarjeta de cuenta y cierre de sesión
            if (uiState.userEmail.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DotWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tu cuenta",
                            style = MaterialTheme.typography.labelLarge,
                            color = DotMutedText
                        )
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = DotInkBlack
                        )
                        Text(
                            text = uiState.userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DotMutedText
                        )
                        OutlinedButton(
                            onClick = viewModel::logout,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = DotRoseStrong,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Cerrar sesión", color = DotRoseStrong)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReminderConfigCard(
    reminder: NotificationReminder,
    onToggle: (Boolean) -> Unit,
    onAdjustHour: (Int) -> Unit,
    onAdjustMinute: (Int) -> Unit
) {
    val isPill = reminder.type == ReminderType.DAILY_CONTRACEPTIVE_PILL

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DotWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isPill) DotBlushPink else DotBlushSurface,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPill) Icons.Outlined.Medication else Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = DotRoseStrong,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.type.defaultTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = DotInkBlack
                    )
                    Text(
                        text = reminder.type.defaultDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DotMutedText
                    )
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DotWhite,
                        checkedTrackColor = DotRoseStrong
                    )
                )
            }

            if (reminder.isEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DotBlushSurface)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hora programada",
                        style = MaterialTheme.typography.labelLarge,
                        color = DotDeepSlate
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { onAdjustHour(-1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Hora -1", tint = DotDeepSlate)
                        }
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", reminder.hour, reminder.minute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DotRoseStrong
                        )
                        IconButton(onClick = { onAdjustHour(1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Outlined.Add, contentDescription = "Hora +1", tint = DotDeepSlate)
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                        OutlinedButton(
                            onClick = { onAdjustMinute(15) },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("+15m", style = MaterialTheme.typography.labelSmall, color = DotDeepSlate)
                        }
                    }
                }
            }
        }
    }
}
