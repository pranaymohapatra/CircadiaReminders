package com.circadia.reminders.entities

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@MappedEntity("refresh_tokens")
data class RefreshTokenEntity(
    @field:Id
    @GeneratedValue // Default is Type.AUTO, which works for SERIAL
    val id: Long? = null,

    val username: String, // Correctly stores User's UUID as String
    val refreshToken: String,
    val revoked: Boolean = false,

    @field:DateCreated
    val dateCreated: Instant? = null
)