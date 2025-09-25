package com.jotadev.aiapaec.ui.screens.exams

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.ScreenTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Exámenes",
                subtitle = "Gestión de exámenes y evaluaciones"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Contenido de exámenes próximamente...")
        }
    }
}