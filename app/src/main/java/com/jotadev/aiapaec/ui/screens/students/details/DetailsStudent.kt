package com.jotadev.aiapaec.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.domain.models.Quiz
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.screens.students.details.StudentDetailsSkeleton

@Composable
fun DetailsStudent(navController: NavController, studentId: Int) {
    val vm: StudentDetailsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    LaunchedEffect(studentId) {
        if (studentId > 0) vm.load(studentId)
    }

    Scaffold { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> {
                    StudentDetailsSkeleton(isSmallScreen = isSmallScreen)
                }
                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage ?: "Error",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.load(studentId) }) { Text("Reintentar") }
                    }
                }
                else -> {
                    val student = state.student
                    if (student != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(if (isSmallScreen) 10.dp else 16.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 16.dp)
                        ) {
                            item {
                                ProfileHeader(
                                    name = "${student.firstName} ${student.lastName}",
                                    email = student.email ?: "Sin correo",
                                    initial = student.firstName.firstOrNull()?.uppercaseChar() ?: 'S',
                                    gender = student.gender,
                                    religion = student.religion,
                                    enrollmentDate = student.enrollmentDate,
                                    phone = student.phone,
                                    isSmallScreen = isSmallScreen
                                )
                            }

                            item {
                                StudentInfoCard(
                                    className = student.className,
                                    classId = student.classId,
                                    studentId = student.id,
                                    isSmallScreen = isSmallScreen
                                )
                            }

                            item {
                                ExamsSection(
                                    exams = state.exams,
                                    onExamClick = { exam -> 
                                        navController.navigate(NavigationRoutes.applyExam(exam.id.toString()))
                                    },
                                    isSmallScreen = isSmallScreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    initial: Char,
    gender: String?,
    religion: String?,
    enrollmentDate: String?,
    phone: String?,
    isSmallScreen: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isSmallScreen) 16.dp else 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(if (isSmallScreen) 16.dp else 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSmallScreen) 60.dp else 80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = if (isSmallScreen) 28.sp else 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(if (isSmallScreen) 8.dp else 12.dp))
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = if (isSmallScreen) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = email,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        fontSize = if (isSmallScreen) 12.sp else 14.sp
                    )
                }
                Spacer(Modifier.height(if (isSmallScreen) 12.dp else 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)) {
                        AssistChip(
                            modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 36.dp else 48.dp),
                            onClick = {},
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = "Sexo: ${gender ?: "-"}",
                                    fontSize = if (isSmallScreen) 10.sp else 14.sp
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        AssistChip(
                            modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 36.dp else 48.dp),
                            onClick = {},
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.CalendarToday, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = "Admisión: ${enrollmentDate ?: "-"}",
                                    fontSize = if (isSmallScreen) 10.sp else 14.sp
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)) {
                        AssistChip(
                            modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 36.dp else 48.dp),
                            onClick = {},
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Favorite, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = "Religión: ${religion ?: "-"}",
                                    fontSize = if (isSmallScreen) 10.sp else 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        AssistChip(
                            modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 36.dp else 48.dp),
                            onClick = {},
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Phone, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = "Tel: ${phone ?: "-"}",
                                    fontSize = if (isSmallScreen) 10.sp else 14.sp
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentInfoCard(
    className: String?,
    classId: Int?,
    studentId: Int,
    isSmallScreen: Boolean
) {
    Text(
        modifier = Modifier.padding(bottom = 4.dp), 
        text = "Grado Asociado:", 
        style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, 
        color = MaterialTheme.colorScheme.onSurface
    )
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(if (isSmallScreen) 12.dp else 16.dp), 
            verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isSmallScreen) 16.dp else 24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = className?.let { "Grado: $it" } ?: "Grado: Sin asignar",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isSmallScreen) 12.sp else 14.sp
                )
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = {},
                    label = { 
                        Text(
                            "ID de Grado| ${classId ?: "-" }",
                            fontSize = if (isSmallScreen) 10.sp else 14.sp
                        ) 
                    },
                    modifier = Modifier.height(if (isSmallScreen) 24.dp else 32.dp)
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text(
                text = "ID estudiante: $studentId", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (isSmallScreen) 12.sp else 14.sp
            )
        }
    }
}

@Composable
private fun ExamsSection(exams: List<Quiz>, onExamClick: (Quiz) -> Unit, isSmallScreen: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Semanales asignados",
            style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (exams.isEmpty()) {
            Text(
                text = "No tiene semanales asignados",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = if (isSmallScreen) 12.sp else 14.sp
            )
        } else {
            exams.forEach { exam -> 
                ExamItem(
                    exam = exam, 
                    onClick = { onExamClick(exam) },
                    isSmallScreen = isSmallScreen
                ) 
            }
        }
    }
}

@Composable
private fun ExamItem(exam: Quiz, onClick: () -> Unit, isSmallScreen: Boolean) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(if (isSmallScreen) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSmallScreen) 32.dp else 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (exam.weekNumber ?: "?").toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSmallScreen) 12.sp else 16.sp
                )
            }
            Spacer(Modifier.width(if (isSmallScreen) 8.dp else 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Semanal N° ${exam.weekNumber ?: ""}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = if (isSmallScreen) 12.sp else 14.sp
                )
                
                val parts = mutableListOf<String>()
                exam.gradoNombre?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
                exam.seccionNombre?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
                exam.bimesterName?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
                exam.unidadId?.let { parts.add("UNIDAD $it") }
                val subtitle = parts.joinToString(" - ")
                
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (isSmallScreen) 10.sp else 12.sp
                    )
                }
            }
            Text(
                text = (exam.numQuestions ?: 0).toString(),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isSmallScreen) 12.sp else 16.sp
            )
        }
    }
}
