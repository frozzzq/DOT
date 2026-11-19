package com.dot.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dot.app.presentation.theme.DotBlushCanvas
import com.dot.app.presentation.theme.DotBlushPink
import com.dot.app.presentation.theme.DotBlushSurface
import com.dot.app.presentation.theme.DotDeepSlate
import com.dot.app.presentation.theme.DotInkBlack
import com.dot.app.presentation.theme.DotMutedText
import com.dot.app.presentation.theme.DotOutlineSoft
import com.dot.app.presentation.theme.DotRoseStrong
import com.dot.app.presentation.theme.DotWhite

@Composable
fun AuthScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigationTarget) {
        when (uiState.navigationTarget) {
            AuthNavigationTarget.GoToOnboarding -> onNavigateToOnboarding()
            AuthNavigationTarget.GoToHome -> onNavigateToHome()
            AuthNavigationTarget.None -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DotBlushPink,
                        DotBlushSurface,
                        DotBlushCanvas
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Emblema minimalista de Dot
            Surface(
                shape = CircleShape,
                color = DotRoseStrong,
                shadowElevation = 6.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "dot.",
                        color = DotWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenida a Dot",
                style = MaterialTheme.typography.headlineLarge,
                color = DotInkBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tu ciclo menstrual claro, privado y sin complicaciones.",
                style = MaterialTheme.typography.bodyLarge,
                color = DotDeepSlate,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DotWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(DotBlushSurface)
                            .padding(4.dp)
                    ) {
                        AuthModePill(
                            text = "Iniciar sesión",
                            selected = uiState.mode == AuthMode.LOGIN,
                            onClick = { viewModel.onModeSelected(AuthMode.LOGIN) },
                            modifier = Modifier.weight(1f)
                        )
                        AuthModePill(
                            text = "Crear cuenta",
                            selected = uiState.mode == AuthMode.REGISTER,
                            onClick = { viewModel.onModeSelected(AuthMode.REGISTER) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = uiState.mode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChanged,
                            label = { Text("¿Cómo te llamas?") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = DotRoseStrong
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(16.dp),
                            colors = dotTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = DotRoseStrong
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = dotTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = DotRoseStrong
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = "Mostrar contraseña",
                                    tint = DotMutedText
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = dotTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    uiState.errorMessage?.let { error ->
                        Surface(
                            color = DotBlushPink.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DotInkBlack,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = viewModel::submit,
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DotRoseStrong,
                            contentColor = DotWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = DotWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = if (uiState.mode == AuthMode.LOGIN) {
                                    "Entrar"
                                } else {
                                    "Crear mi cuenta"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = DotWhite
                            )
                        }
                    }

                    Text(
                        text = if (uiState.mode == AuthMode.LOGIN) {
                            "¿Primera vez en Dot? Cambia a \"Crear cuenta\" arriba."
                        } else {
                            "¿Ya tienes cuenta? Cambia a \"Iniciar sesión\" arriba."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = DotMutedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = DotWhite.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = DotDeepSlate,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "100% gratuito • Datos cifrados en tu dispositivo",
                        style = MaterialTheme.typography.labelMedium,
                        color = DotDeepSlate
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthModePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) DotRoseStrong else DotBlushSurface,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) DotWhite else DotDeepSlate,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun dotTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DotRoseStrong,
    unfocusedBorderColor = DotOutlineSoft,
    focusedLabelColor = DotRoseStrong,
    unfocusedLabelColor = DotMutedText,
    focusedTextColor = DotInkBlack,
    unfocusedTextColor = DotInkBlack,
    cursorColor = DotRoseStrong
)
