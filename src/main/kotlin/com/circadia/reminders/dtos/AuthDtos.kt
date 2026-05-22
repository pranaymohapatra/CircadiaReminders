package com.circadia.reminders.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class RefreshTokenRequest(
    @JsonProperty("refresh_token")
    val refreshToken: String
)
