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

    //Secondary Screens Details
    const val APPLY_EXAM = "apply_exam/{examId}"
    const val DETAILS_STUDENT = "student_detail/{studentId}"
    const val DETAILS_CLASS = "class_detail/{classId}"
    const val QUIZ_ANSWERS = "quiz_answers/{examId}"


    
    // Secondary Screens
    const val EXAM_DETAIL = "exam_detail/{examId}"
    const val CREATE_EXAM = "create_exam"
    const val SCAN_CARD = "scan_card"
    const val SCAN_UPLOAD = "scan_upload"
    const val GROUP_CLASSES = "group_classes"
    const val CROP_PREVIEW = "crop_preview"
    const val SCAN_RESULT = "scan_result"

    
    // Navigation with arguments
    fun examDetail(examId: String) = "exam_detail/$examId"
    fun applyExam(examId: String) = "apply_exam/$examId"
    fun quizAnswers(examId: String) = "quiz_answers/$examId"
    fun detailsStudent(studentId: Int) = "student_detail/$studentId"
    fun detailsClass(classId: Int) = "class_detail/$classId"
    fun cropPreview(path: String) = "${CROP_PREVIEW}?path=$path"
    fun scanResult(runId: String, overlayUrl: String, tipo: Int) = "${SCAN_RESULT}?run_id=$runId&overlay=$overlayUrl&tipo=$tipo"
}