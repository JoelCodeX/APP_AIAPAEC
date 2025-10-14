package com.jotadev.aiapaec.ui.screens.classes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.ui.components.CustomTopAppBar
import com.jotadev.aiapaec.ui.components.StudentsList
import com.jotadev.aiapaec.navigation.NavigationRoutes

@Composable
fun DetailsClasses(
    navController: NavController,
    classId: String
) {
    val vm: ClassStudentsViewModel = viewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(classId) {
        classId.toIntOrNull()?.let { vm.load(it) }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Estudiantes de la clase",
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
//                .padding(16.dp)
        ) {
            // BUSQUEDA POR ID O NOMBRE
            OutlinedTextField(
                value = uiState.query,
                onValueChange = {
                    vm.onQueryChange(it)
                    vm.refresh()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( 16.dp),
                placeholder = { Text("Buscar por nombre o ID...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            StudentsList(
                students = uiState.students,
                onStudentClick = { student ->
                    navController.navigate(NavigationRoutes.detailsStudent(student.id))
                }
            )
        }
    }
}