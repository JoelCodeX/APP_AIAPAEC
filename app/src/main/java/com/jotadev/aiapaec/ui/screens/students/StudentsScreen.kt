package com.jotadev.aiapaec.ui.screens.students

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("Todas las clases") }
    
    // DATOS SIMULADOS DE ESTUDIANTES
    val allStudents = remember {
        listOf(
            Student("EST001", "Juan Carlos", "Pérez García", "1° Primaria"),
            Student("EST002", "María Elena", "López Martínez", "1° Primaria"),
            Student("EST003", "Carlos Alberto", "Rodríguez Silva", "2° Primaria"),
            Student("EST004", "Ana Sofía", "González Herrera", "2° Primaria"),
            Student("EST005", "Luis Fernando", "Sánchez Torres", "3° Primaria"),
            Student("EST006", "Isabella", "Ramírez Castro", "3° Primaria"),
            Student("EST007", "Diego Alejandro", "Morales Vega", "4° Primaria"),
            Student("EST008", "Valentina", "Jiménez Ruiz", "4° Primaria"),
            Student("EST009", "Santiago", "Vargas Mendoza", "5° Primaria"),
            Student("EST010", "Camila Andrea", "Cruz Delgado", "5° Primaria"),
            Student("EST011", "Sebastián", "Ortega Flores", "6° Primaria"),
            Student("EST012", "Sofía Alejandra", "Restrepo Gómez", "6° Primaria"),
            Student("EST013", "Andrés Felipe", "Muñoz Cardenas", "1° Secundaria"),
            Student("EST014", "Gabriela", "Pineda Rojas", "1° Secundaria"),
            Student("EST015", "Nicolás", "Aguilar Moreno", "2° Secundaria")
        )
    }
    
    // LOGICA DE FILTRADO
    val filteredStudents = remember(searchQuery, selectedClass, allStudents) {
        allStudents.filter { student ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                student.firstName.contains(searchQuery, ignoreCase = true) ||
                student.lastName.contains(searchQuery, ignoreCase = true) ||
                student.id.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesClass = selectedClass == "Todas las clases" || student.className == selectedClass
            
            matchesSearch && matchesClass
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            ScreenTopAppBar(
                screenTitle = "Estudiantes",
                subtitle = "Listado de estudiantes registrados"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // BARRA DE BUSQUEDA Y FILTROS
            StudentsSearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedClass = selectedClass,
                onClassChange = { selectedClass = it }
            )
            
            // LISTA DE ESTUDIANTES
            StudentsList(
                students = filteredStudents,
                modifier = Modifier.weight(1f)
            )
        }
    }
}