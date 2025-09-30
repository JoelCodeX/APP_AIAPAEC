package com.jotadev.aiapaec.ui.screens.classes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("Todos los niveles") }
    
    // DATOS SIMULADOS DE CLASES
    val allClasses = remember {
        listOf(
            ClassInfo("1A", "Primer Grado A", "Primaria", 25),
            ClassInfo("1B", "Primer Grado B", "Primaria", 23),
            ClassInfo("2A", "Segundo Grado A", "Primaria", 27),
            ClassInfo("3A", "Tercer Grado A", "Primaria", 24),
            ClassInfo("4A", "Cuarto Grado A", "Primaria", 26),
            ClassInfo("5A", "Quinto Grado A", "Primaria", 22),
            ClassInfo("6A", "Sexto Grado A", "Primaria", 28),
            ClassInfo("1S", "Primero de Secundaria", "Secundaria", 30),
            ClassInfo("2S", "Segundo de Secundaria", "Secundaria", 29),
            ClassInfo("3S", "Tercero de Secundaria", "Secundaria", 31),
            ClassInfo("4S", "Cuarto de Secundaria", "Secundaria", 28),
            ClassInfo("5S", "Quinto de Secundaria", "Secundaria", 27)
        )
    }
    
    // FILTRADO DE CLASES
    val filteredClasses = remember(searchQuery, selectedLevel) {
        allClasses.filter { classInfo ->
            val matchesSearch = searchQuery.isBlank() || 
                classInfo.name.contains(searchQuery, ignoreCase = true) ||
                classInfo.id.contains(searchQuery, ignoreCase = true)
            
            val matchesLevel = selectedLevel == "Todos los niveles" || 
                classInfo.level == selectedLevel
            
            matchesSearch && matchesLevel
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Clases",
                subtitle = "Gestión de clases del centro educativo"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // BARRA DE BUSQUEDA Y FILTROS
            ClassesSearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedLevel = selectedLevel,
                onLevelChange = { selectedLevel = it }
            )
            
            // LISTA DE CLASES
            ClassesList(
                classes = filteredClasses,
                modifier = Modifier.weight(1f)
            )
        }
    }
}