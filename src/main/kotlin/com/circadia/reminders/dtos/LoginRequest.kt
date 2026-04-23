package com.circadia.reminders.dtos

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class LoginRequest(
    val email: String,
    val password: String
)
