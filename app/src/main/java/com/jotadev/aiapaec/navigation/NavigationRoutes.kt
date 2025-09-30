package com.jotadev.aiapaec.navigation

object NavigationRoutes {
    const val LOGIN = "login"
    const val MAIN = "main"
    
    // Bottom Navigation Routes
    const val HOME = "home"
    const val EXAMS = "exams"
    const val CLASSES = "classes"
    const val STUDENTS = "students"
    const val RESULTS = "results"
    const val SETTINGS = "settings"

    
    // Secondary Screens
    const val EXAM_DETAIL = "exam_detail/{examId}"
    const val CREATE_EXAM = "create_exam"
    const val SCAN_CARD = "scan_card"
    const val GROUP_CLASSES = "group_classes"
    
    // Navigation with arguments
    fun examDetail(examId: String) = "exam_detail/$examId"
}