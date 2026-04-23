package com.circadia.reminders.dtos

import com.circadia.reminders.domain.TaskStatus
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.serde.annotation.Serdeable
import java.time.OffsetDateTime
import java.util.*

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReminderDto(
    val id: UUID,
    val userId: UUID,
    val projectId: UUID?,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val dueDate: OffsetDateTime?,
    val contextTags: List<String>,
    val priority: Int,
    val isRecurring: Boolean,
    val completedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val recurrence: RecurrenceDto,
    val endCondition: EndConditionDto?
)

@Serdeable
data class CreateReminderDto(
    val projectId: UUID?,
    val title: String,
    val description: String?,
    val dueDate: OffsetDateTime?,
    val contextTags: List<String> = emptyList(),
    val priority: Int = 0,
    val isRecurring: Boolean = false,
    val recurrence: RecurrenceDto?,
    val endCondition: EndConditionDto?
)

@Serdeable
data class UpdateReminderDto(
    val projectId: UUID?,
    val title: String?,
    val description: String?,
    val status: TaskStatus?,
    val dueDate: OffsetDateTime?,
    val contextTags: List<String>?,
    val priority: Int?,
    val isRecurring: Boolean?,
    val recurrence: RecurrenceDto?
)
