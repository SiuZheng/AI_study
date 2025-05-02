package com.example.aistudy.config

object Config {
    const val API_BASE_URL = "https://50f0-2001-d08-e0-1cb5-11c1-d813-165d-9555.ngrok-free.app" // Replace this with your actual API URL
    const val PREDICT_ENDPOINT = "predict"
    const val DOCTOR_PREDICT_ENDPOINT = "predict/doctor"
    const val CHAT_ENDPOINT = "chat"
    // API Timeouts (in seconds)
    const val CONNECT_TIMEOUT = 60L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 60L
}