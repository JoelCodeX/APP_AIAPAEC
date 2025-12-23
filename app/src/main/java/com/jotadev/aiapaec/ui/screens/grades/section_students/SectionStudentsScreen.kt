package com.jotadev.aiapaec.ui.screens.grades.section_students

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jotadev.aiapaec.navigation.NavigationRoutes
import com.jotadev.aiapaec.ui.components.students.StudentsList
import com.jotadev.aiapaec.ui.components.students.StudentsSearchAndFilterBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SectionStudentsScreen(
    navController: NavController,
    gradeId: Int,
    sectionId: Int,
    gradeName: String,
    sectionName: String,
    studentCount: Int
) {
    val vm: SectionStudentsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gradeId, sectionId) {
        vm.init(gradeId, sectionId)
    }

    val pullState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { vm.refresh() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar only
            StudentsSearchAndFilterBar(
                searchQuery = state.query,
                onSearchQueryChange = {
                    vm.onQueryChange(it)
                    vm.fetchStudents(page = 1)
                },
                selectedGrade = null,
                onGradeChange = {},
                gradeOptions = emptyList(),
                selectedSection = null,
                onSectionChange = {},
                sectionOptions = emptyList(),
                isMetaLoading = false,
                showFilters = false
            )

            StudentsList(
                students = state.students,
                modifier = Modifier.weight(1f),
                onStudentClick = { student ->
                    navController.navigate(NavigationRoutes.detailsStudent(student.id))
                }
            )

            if (state.isLoading && state.students.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        PullRefreshIndicator(
            refreshing = state.isLoading,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}
