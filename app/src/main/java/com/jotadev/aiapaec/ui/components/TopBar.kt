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
import androidx.wear.compose.material3.ColorScheme
import com.jotadev.aiapaec.ui.theme.Crimson100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Crimson100,
    contentColor: Color = Color.White,
) {
    TopAppBar(
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor
        ),
        modifier = modifier
    )
}

@Composable
fun WelcomeTopAppBar(
    userName: String,
    subtitle: String = "Sistema de gestión de exámenes AIAPAEC",
    backgroundColor: Color = Crimson100,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    CustomTopAppBar(
        title = "¡Bienvenido, $userName!",
        subtitle = subtitle,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
    )
}

@Composable
fun ScreenTopAppBar(
    screenTitle: String,
    subtitle: String? = null,
    backgroundColor: Color = Crimson100,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    CustomTopAppBar(
        title = screenTitle,
        subtitle = subtitle,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
    )
}