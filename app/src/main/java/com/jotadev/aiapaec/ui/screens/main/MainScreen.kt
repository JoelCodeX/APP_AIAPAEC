package com.jotadev.aiapaec.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jotadev.aiapaec.navigation.BottomNavItem
import com.jotadev.aiapaec.navigation.BottomNavigationBar
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.ScreenTopAppBar
import com.jotadev.aiapaec.ui.components.WelcomeTopAppBar
import com.jotadev.aiapaec.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.delay

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
        NavigationRoutes.CROP_PREVIEW
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            MainTopBar(
                currentRoute = currentRoute,
                onOpenSettings = { showSettings = true }
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

            SettingsSidePanel(
                show = showSettings,
                onClose = { showSettings = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingsScreen(navController = navController, onClose = { showSettings = false })
            }
        }
    }
}

@Composable
private fun MainTopBar(
    currentRoute: String?,
    onOpenSettings: () -> Unit
) {
    when {
        currentRoute?.startsWith(NavigationRoutes.HOME) == true -> {
            WelcomeTopAppBar(onNavigationClick = onOpenSettings)
        }
        currentRoute?.startsWith(NavigationRoutes.EXAMS) == true -> {
            ScreenTopAppBar(screenTitle = "Exámenes")
        }
        currentRoute?.startsWith(NavigationRoutes.CLASSES) == true -> {
            ScreenTopAppBar(screenTitle = "Clases")
        }
        currentRoute?.startsWith(NavigationRoutes.STUDENTS) == true -> {
            ScreenTopAppBar(screenTitle = "Estudiantes")
        }
        currentRoute?.startsWith(NavigationRoutes.RESULTS) == true -> {
            ScreenTopAppBar(screenTitle = "Resultados")
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
        Box(modifier = modifier) {
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
                        .fillMaxWidth(0.7f)
                        .background(MaterialTheme.colorScheme.onPrimary)
                ) {
                    content()
                }
            }
        }
    }
}