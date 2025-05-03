package com.example.aistudy.api

data class StudyPlanResponse(
    val study_plan: List<DailyPlan>
)

data class DailyPlan(
    val date: String,
    val tasks: List<Task>
)

data class Task(
    val subject: String,
    val duration: Int
) 