package com.circadia.reminders.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.serde.annotation.Serdeable
import java.time.OffsetDateTime
import java.util.*

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProjectDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
    val color: String?,
    val isArchived: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

@Serdeable
data class CreateProjectDto(
    val name: String,
    val description: String?,
    val color: String?
)

@Serdeable
data class UpdateProjectDto(
    val name: String?,
    val description: String?,
    val color: String?,
    val isArchived: Boolean?
)

@Serdeable
data class ProjectSummaryDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val color: String?,
    val reminderCount: Long
)
