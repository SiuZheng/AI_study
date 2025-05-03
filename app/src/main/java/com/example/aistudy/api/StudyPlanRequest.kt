package com.example.aistudy.api

data class StudyPlanRequest(
    val step: String = "study planner",
    val planner_prompt: String
) 