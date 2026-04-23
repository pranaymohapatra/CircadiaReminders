package com.circadia.reminders.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.serde.annotation.Serdeable
import java.time.OffsetDateTime
import java.util.*

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

@Serdeable
data class CreateUserDto(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String?,
    val lastName: String?
)

@Serdeable
data class UpdateUserDto(
    val firstName: String?,
    val lastName: String?
)

@Serdeable
data class UserResponseDto(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val createdAt: OffsetDateTime
)
