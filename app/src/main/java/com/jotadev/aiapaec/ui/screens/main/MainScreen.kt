package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jotadev.aiapaec.navigation.BottomNavItem
import com.jotadev.aiapaec.navigation.BottomNavigationBar
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.CustomTopAppBar
import com.jotadev.aiapaec.ui.components.ScreenTopAppBar
import com.jotadev.aiapaec.ui.components.WelcomeTopAppBar
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = BottomNavItem.getAllItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showSettings by remember { mutableStateOf(false) }

    val routesWithoutBottomBar = listOf(
        NavigationRoutes.GROUP_CLASSES,
        NavigationRoutes.EXAM_DETAIL,
        NavigationRoutes.CREATE_EXAM,
        NavigationRoutes.SCAN_CARD,
        NavigationRoutes.SCAN_UPLOAD,
        NavigationRoutes.APPLY_EXAM,
        NavigationRoutes.QUIZ_ANSWERS,
        NavigationRoutes.DETAILS_STUDENT,
        NavigationRoutes.DETAILS_CLASS,
        NavigationRoutes.CROP_PREVIEW,
        NavigationRoutes.WEEKLY
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            MainTopBar(
                currentRoute = currentRoute,
                onOpenSettings = { showSettings = true },
                navController = navController
            )
        },
        bottomBar = {
            if (routesWithoutBottomBar.none { currentRoute?.startsWith(it) == true }) {
                BottomNavigationBar(
                    navController = navController,
                    items = bottomNavItems
                )
            }
        }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                MainNavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    onOpenSettings = { showSettings = true }
                )
            }
        }

        SettingsSidePanel(
            show = showSettings,
            onClose = { showSettings = false },
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsScreen(navController = navController, onClose = { showSettings = false })
        }
    }
}

@Composable
private fun MainTopBar(
    currentRoute: String?,
    onOpenSettings: () -> Unit,
    navController: NavHostController
) {
    when {
        currentRoute?.startsWith(NavigationRoutes.HOME) == true -> {
            WelcomeTopAppBar(onNavigationClick = onOpenSettings)
        }
        currentRoute?.startsWith(NavigationRoutes.EXAMS) == true -> {
            ScreenTopAppBar(
                screenTitle = "Exámenes",
                actions = {
                    Box(modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                                .blur(12.dp)
                        )
                        IconButton(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("exams_create_request", true)
                            },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Crear examen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
        currentRoute?.startsWith(NavigationRoutes.QUIZ_ANSWERS) == true -> {
            val handle = navController.currentBackStackEntry?.savedStateHandle
            val isEditing by (handle?.getStateFlow("answers_is_editing", false) ?: MutableStateFlow(false)).collectAsState()
            val hasChanges by (handle?.getStateFlow("answers_has_changes", false) ?: MutableStateFlow(false)).collectAsState()
            val hasAny by (handle?.getStateFlow("answers_has_any", false) ?: MutableStateFlow(false)).collectAsState()
            CustomTopAppBar(
                title = "Respuestas",
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIcon = {
                    IconButton(onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("answers_back_request", true)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("answers_edit_toggle", true)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar respuestas",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else if (hasChanges) {
                        IconButton(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("answers_save_request", true)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Guardar cambios",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    if (!isEditing && hasAny) {
                        IconButton(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("answers_delete_request", true)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Eliminar solucionario",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
        currentRoute?.startsWith(NavigationRoutes.APPLY_EXAM) == true -> {
            // Subpantalla: usar un único TopBar global con botón de regresar
            CustomTopAppBar(
                title = "Aplicar evaluación",
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
        currentRoute?.startsWith(NavigationRoutes.GRADES) == true -> {
            ScreenTopAppBar(screenTitle = "Grados")
        }
        currentRoute?.startsWith(NavigationRoutes.STUDENTS) == true -> {
            ScreenTopAppBar(screenTitle = "Estudiantes")
        }
        currentRoute?.startsWith(NavigationRoutes.FORMATS) == true -> {
            ScreenTopAppBar(
                screenTitle = "Asiganción de Formatos",
                actions = {
                    Box(modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                                .blur(12.dp)
                        )
                        IconButton(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("formats_create_request", true)
                            },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Asignar formato",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
        currentRoute?.startsWith(NavigationRoutes.WEEKLY) == true -> {
            val weeklyTitle = navController.currentBackStackEntry?.arguments?.getString("title") ?: "Formatos semanales"
            val prevHandle = navController.previousBackStackEntry?.savedStateHandle
            val gradeName = prevHandle?.get<String>("weekly_grade_name") ?: ""
            val sectionName = prevHandle?.get<String>("weekly_section_name") ?: ""
            val weeklySubtitle = when {
                gradeName.isNotBlank() && sectionName.isNotBlank() -> "$gradeName | $sectionName"
                else -> null
            }
            CustomTopAppBar(
                title = weeklyTitle,
                subtitle = weeklySubtitle,
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    Box(modifier = Modifier.size(40.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
                                .blur(12.dp)
                        )
                        IconButton(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("weekly_create_request", true)
                            },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Crear quiz semanal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
        else -> {}
    }
}

@Composable
private fun SettingsSidePanel(
    show: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var renderPopup by remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        if (show) {
            renderPopup = true
        } else {
            delay(200)
            renderPopup = false
        }
    }
    if (renderPopup) {
        Box(modifier = modifier.zIndex(100f)) {
            AnimatedVisibility(
                visible = renderPopup,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.zIndex(0f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .pointerInput(Unit) { detectTapGestures { onClose() } }
                )
            }
            AnimatedVisibility(
                visible = show,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .background(MaterialTheme.colorScheme.onPrimary)
                ) {
                    content()
                }
            }
        }
    }
}
