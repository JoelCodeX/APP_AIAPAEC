package com.jotadev.aiapaec.ui.screens.login

import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.view.Gravity
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jotadev.aiapaec.R


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        com.jotadev.aiapaec.data.storage.UserStorage.init(context)
        viewModel.prefillRememberedEmail()
    }
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            showWelcomeToast(context)
            onLoginSuccess()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.4f)
                quadraticTo(
                    size.width * 0.5f, size.height * 0.55f,
                    0f, size.height * 0.4f
                )
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFAB2524)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            LogoAiapaec()
            Spacer(modifier = Modifier.height(28.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Inicia sesión para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val fieldErrors = mapFieldErrorsFromBackend(uiState.errorMessage)
                    CampoTextoUsuario(
                        valor = uiState.usuario,
                        onValorCambiado = viewModel::updateUsuario,
                        serverErrorText = fieldErrors.email
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CampoTextoContrasena(
                        valor = uiState.contrasena,
                        onValorCambiado = viewModel::updateContrasena,
                        mostrarContrasena = uiState.mostrarContrasena,
                        onToggleVisibilidad = viewModel::toggleMostrarContrasena,
                        serverErrorText = fieldErrors.password
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = uiState.recordarUsuario,
                                onCheckedChange = { viewModel.setRecordarUsuario(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.secondary,
                                    uncheckedColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                            Text(
                                text = "Recordar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                onForgotPassword()
                            }
                        )
                    }
                    uiState.errorMessage?.let { error ->
                        val showGlobalError = error.contains("ERROR", ignoreCase = true) ||
                                error.contains("SERVIDOR", ignoreCase = true) ||
                                error.contains("CONEXIÓN", ignoreCase = true) ||
                                error.contains("RESPUESTA VACÍA", ignoreCase = true)
                        if (showGlobalError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    val isLoginEnabled = viewModel.isValidEmail(uiState.usuario) && uiState.contrasena.length >= 6
                    BotonIngresar(
                        onClick = {
                            viewModel.clearError()
                            viewModel.login()
                        },
                        isLoading = uiState.isLoading,
                        isEnabled = isLoginEnabled
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            SocialMediaButtons()
        }
    }
}

// Modela errores de backend hacia los campos
private data class FieldErrors(val email: String?, val password: String?)

// Mapea variantes de mensajes del backend a ayudas de campo
private fun mapFieldErrorsFromBackend(error: String?): FieldErrors {
    if (error.isNullOrBlank()) return FieldErrors(null, null)
    val e = error.uppercase()
    val emailPatterns = listOf(
        "USUARIO NO EXISTE",
        "EMAIL INCORRECTO",
        "CORREO INCORRECTO",
        "EMAIL NO REGISTRADO",
        "USUARIO NO REGISTRADO",
        "CORREO NO REGISTRADO",
        "USUARIO NO ENCONTRADO",
        "EMAIL NOT FOUND",
        "USER NOT FOUND"
    )
    val passwordPatterns = listOf(
        "CONTRASEÑA INCORRECTA",
        "PASSWORD INCORRECTA",
        "CLAVE INCORRECTA",
        "INVALID PASSWORD",
        "WRONG PASSWORD"
    )
    val emailError = if (emailPatterns.any { e.contains(it) }) "Email incorrecto" else null
    val passwordError = if (passwordPatterns.any { e.contains(it) }) "Contraseña incorrecta" else null
    return FieldErrors(emailError, passwordError)
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun SocialMediaButtons() {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Síguenos en nuestras redes sociales",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialMediaButton(
                iconResId = R.drawable.facebook, // Asegúrate de tener este recurso
                contentDescription = "Facebook",
                onClick = {
                    openUrl(
                        context,
                        "https://www.facebook.com/colegios.aiapaec?locale=es_LA"
                    )
                }
            )
            SocialMediaButton(
                iconResId = R.drawable.instagram, // Asegúrate de tener este recurso
                contentDescription = "Instagram",
                onClick = { openUrl(context, "https://www.instagram.com/colegios.aiapaec/") }
            )

            SocialMediaButton(
                iconResId = R.drawable.tiktok,
                contentDescription = "TikTok",
                onClick = { openUrl(context, "https://www.tiktok.com/@colegios.aiapaec") }
            )

            SocialMediaButton(
                iconResId = R.drawable.youtube,
                contentDescription = "YouTube",
                onClick = { openUrl(context, "https://www.youtube.com/@colegiosaiapaec") }
            )
        }
    }
}

@Composable
fun SocialMediaButton(
    onClick: () -> Unit,
    iconResId: Int,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun LogoAiapaec() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo AIAPAEC",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
private fun CampoTextoUsuario(
    valor: String,
    onValorCambiado: (String) -> Unit,
    serverErrorText: String? = null,
) {
    var touched by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isEmpty = valor.isBlank()
    val isInvalid = !isEmpty && !android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()
    val showError = touched && (isEmpty || isInvalid)
    val localHelper = when {
        touched && isEmpty -> "El email es obligatorio"
        touched && isInvalid -> "Ingresa un email válido"
        else -> null
    }
    val helperText = localHelper ?: serverErrorText
    OutlinedTextField(
        value = valor,
        onValueChange = {
            if (!touched) touched = true
            onValorCambiado(it)
        },
        label = { Text("Email") },
        placeholder = { Text("") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = "Icono de email",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
        ),
        isError = showError || (serverErrorText != null),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    AnimatedVisibility(
        visible = helperText != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = helperText ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun CampoTextoContrasena(
    valor: String,
    onValorCambiado: (String) -> Unit,
    mostrarContrasena: Boolean,
    onToggleVisibilidad: () -> Unit,
    serverErrorText: String? = null
) {
    var touched by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isEmpty = valor.isBlank()
    val isTooShort = !isEmpty && valor.length < 6
    val localHelper = when {
        touched && isEmpty -> "La contraseña es obligatoria"
        touched && isTooShort -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
    val helperText = localHelper ?: serverErrorText
    OutlinedTextField(
        value = valor,
        onValueChange = {
            if (!touched) touched = true
            onValorCambiado(it)
        },
        label = { Text("Contraseña") },
        placeholder = { Text("") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Icono de contraseña",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
        ),
        isError = (touched && (isEmpty || isTooShort)) || (serverErrorText != null),
        visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibilidad) {
                Icon(
                    imageVector = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
    AnimatedVisibility(
        visible = helperText != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = helperText ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun BotonIngresar(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = Color.Gray.copy(0.4f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onSecondary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Ingresar",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Icono de ingresar",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(
                    start = 8.dp
                )
            )
        }
    }
}
// Muestra un Toast de bienvenida con el nombre del usuario
private fun showWelcomeToast(
    context: Context,
    containerColor: Color? = null,
    textColor: Color? = null
) {
    val name = com.jotadev.aiapaec.data.storage.UserStorage.getName()
    val saludo = if (!name.isNullOrBlank()) "¡Bienvenido, $name!" else "¡Bienvenido!"

    val bgColor = (containerColor ?: Color(0xFFFDC400)).toArgb()
    val fgColor = (textColor ?: Color.Black).toArgb()
    val iconColor = Color(0xFF2E7D32).toArgb()

    val radiusPx = (12f * context.resources.displayMetrics.density)
    val drawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radiusPx
        setColor(bgColor)
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        background = drawable
        setPadding(
            (16 * context.resources.displayMetrics.density).toInt(),
            (10 * context.resources.displayMetrics.density).toInt(),
            (16 * context.resources.displayMetrics.density).toInt(),
            (10 * context.resources.displayMetrics.density).toInt()
        )
        gravity = Gravity.CENTER_VERTICAL
    }

    val iconView = TextView(context).apply {
        text = "✔"
        setTextColor(iconColor)
        textSize = 18f
    }
    val iconLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setMargins(0, 0, (8 * context.resources.displayMetrics.density).toInt(), 0)
    }
    container.addView(iconView, iconLp)

    val messageView = TextView(context).apply {
        text = saludo
        setTextColor(fgColor)
        textSize = 16f
    }
    container.addView(messageView)

    val toast = Toast(context)
    toast.view = container
    toast.duration = Toast.LENGTH_SHORT
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, (100 * context.resources.displayMetrics.density).toInt())
    toast.show()
}
