package com.jotadev.aiapaec.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jotadev.aiapaec.ui.theme.Crimson100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = Color.White,
    actions: @Composable RowScope.() -> Unit = {} // ✅ soporte para contenido adicional
) {
    TopAppBar(
        modifier= modifier,
        title = {
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = contentColor.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = actions, // ✅ se usa aquí
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor
        ),
    )
}

@Composable
fun WelcomeTopAppBar(
    subtitle: String = "Sistema de gestión de exámenes AIAPAEC",
    backgroundColor:Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {} // ✅ se define aquí también
) {
    CustomTopAppBar(
        title = "Bienvenido",
        subtitle = subtitle,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
        actions = actions // ✅ se pasa correctamente
    )
}

@Composable
fun ScreenTopAppBar(
    screenTitle: String,
    subtitle: String? = null,
    backgroundColor: Color= MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {} // ✅ opcional para otras pantallas también
) {
    CustomTopAppBar(
        title = screenTitle,
        subtitle = subtitle,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
        actions = actions
    )
}