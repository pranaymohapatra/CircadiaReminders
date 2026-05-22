package com.circadia.reminders.dtos.mappers

import com.circadia.reminders.dtos.*
import com.circadia.reminders.domain.UserProvider
import com.circadia.reminders.entities.*
import io.micronaut.json.JsonMapper
import jakarta.inject.Singleton
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Singleton
class EntityMapper(
    private val jsonMapper: JsonMapper
) {
    
    fun toUserDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            firstName = entity.firstName,
            lastName = entity.lastName,
            createdAt = requireNotNull(entity.createdAt) { "User createdAt is required" },
            updatedAt = requireNotNull(entity.updatedAt) { "User updatedAt is required" }
        )
    }
    
    fun toUserResponseDto(entity: UserEntity): UserResponseDto {
        return UserResponseDto(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            firstName = entity.firstName,
            lastName = entity.lastName,
            createdAt = requireNotNull(entity.createdAt) { "User createdAt is required" }
        )
    }
    
    fun toProjectDto(entity: ProjectEntity): ProjectDto {
        return ProjectDto(
            id = requireNotNull(entity.id) { "Project id is required" },
            userId = entity.userId,
            name = entity.name,
            description = entity.description,
            color = entity.color,
            isArchived = entity.isArchived,
            createdAt = requireNotNull(entity.createdAt) { "Project createdAt is required" },
            updatedAt = requireNotNull(entity.updatedAt) { "Project updatedAt is required" }
        )
    }
    
    fun toProjectSummaryDto(entity: ProjectEntity, reminderCount: Long): ProjectSummaryDto {
        return ProjectSummaryDto(
            id = requireNotNull(entity.id) { "Project id is required" },
            name = entity.name,
            description = entity.description,
            color = entity.color,
            reminderCount = reminderCount
        )
    }
    
    fun toReminderDto(entity: ReminderEntity): ReminderDto {
        return ReminderDto(
            id = entity.id,
            userId = entity.userId,
            projectId = entity.projectId,
            title = entity.title,
            description = entity.description,
            status = entity.status,
            dueDate = entity.dueDate,
            contextTags = entity.contextTags.toList(),
            priority = entity.priority,
            isRecurring = entity.isRecurring,
            version = entity.version,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            recurrence = toRecurrenceDto(entity),
            endCondition = toEndConditionDto(entity.endCondition)
        )
    }
    
    fun toReminderEntity(createDto: CreateReminderDto, userId: UUID, now: OffsetDateTime): ReminderEntity {
        val recurrence = createDto.recurrence ?: throw IllegalArgumentException("recurrence is required")
        return when (recurrence) {
            is StaticRecurrenceDto -> toReminderEntity(createDto, userId, now, recurrence)
            is DynamicRecurrenceDto -> toReminderEntity(createDto, userId, now, recurrence)
        }
    }

    fun applyReminderUpdate(existing: ReminderEntity, updateDto: UpdateReminderDto, now: OffsetDateTime): ReminderEntity {
        val withRecurrenceApplied = updateDto.recurrence?.let { recurrence ->
            when (recurrence) {
                is StaticRecurrenceDto -> existing.copy(
                    isRecurring = true,
                    reminderType = com.circadia.reminders.domain.ReminderType.STATIC,
                    executionTime = recurrence.executionTime,
                    daysOfWeek = recurrence.daysOfWeek.toTypedArray(),
                    intervalMinutes = null,
                    triggerEvent = null
                )
                is DynamicRecurrenceDto -> existing.copy(
                    isRecurring = true,
                    reminderType = com.circadia.reminders.domain.ReminderType.DYNAMIC,
                    executionTime = recurrence.executionTime,
                    daysOfWeek = recurrence.daysOfWeek?.toTypedArray() ?: emptyArray(),
                    intervalMinutes = recurrence.intervalMinutes,
                    triggerEvent = recurrence.triggerEvent
                )
            }
        } ?: existing

        val endConditionJson = if (updateDto.endCondition != null) fromEndConditionDto(updateDto.endCondition) else withRecurrenceApplied.endCondition

        return withRecurrenceApplied.copy(
            projectId = updateDto.projectId ?: withRecurrenceApplied.projectId,
            title = updateDto.title ?: withRecurrenceApplied.title,
            description = updateDto.description ?: withRecurrenceApplied.description,
            status = updateDto.status ?: withRecurrenceApplied.status,
            dueDate = updateDto.dueDate ?: withRecurrenceApplied.dueDate,
            contextTags = updateDto.contextTags?.toTypedArray() ?: withRecurrenceApplied.contextTags,
            priority = updateDto.priority ?: withRecurrenceApplied.priority,
            endCondition = endConditionJson,
            updatedAt = now
        )
    }



    
    fun toUserEntity(createDto: CreateUserDto, passwordHash: String, now: OffsetDateTime): UserEntity {
        return UserEntity(
            id = UUID.randomUUID(),
            email = createDto.email,
            username = createDto.username,
            passwordHash = passwordHash,
            provider = UserProvider.LOCAL,
            firstName = createDto.firstName,
            lastName = createDto.lastName,
            createdAt = now,
            updatedAt = now
        )
    }
    
    fun toProjectEntity(createDto: CreateProjectDto, userId: UUID, now: OffsetDateTime): ProjectEntity {
        return ProjectEntity(
            id = java.util.UUID.randomUUID(),
            userId = userId,
            name = createDto.name,
            description = createDto.description,
            color = createDto.color,
            isArchived = false,
            createdAt = now,
            updatedAt = now
        )
    }
    
    private fun toReminderEntity(createDto: CreateReminderDto, userId: UUID, now: OffsetDateTime, staticRecurrence: StaticRecurrenceDto): ReminderEntity {
        return ReminderEntity(
            id = java.util.UUID.randomUUID(),
            userId = userId,
            projectId = createDto.projectId,
            title = createDto.title,
            description = createDto.description,
            status = com.circadia.reminders.domain.TaskStatus.PAUSED,
            dueDate = createDto.dueDate,
            contextTags = createDto.contextTags.toTypedArray(),
            priority = createDto.priority,
            isRecurring = true,
            completedAt = null,
            deletedAt = null,
            version = 0,
            createdAt = now,
            updatedAt = now,
            reminderType = com.circadia.reminders.domain.ReminderType.STATIC,
            executionTime = staticRecurrence.executionTime,
            daysOfWeek = staticRecurrence.daysOfWeek.toTypedArray(),
            intervalMinutes = null,
            triggerEvent = null,
            endCondition = fromEndConditionDto(createDto.endCondition)
        )
    }
    
    private fun toReminderEntity(createDto: CreateReminderDto, userId: UUID, now: OffsetDateTime, dynamicRecurrence: DynamicRecurrenceDto): ReminderEntity {
        return ReminderEntity(
            id = java.util.UUID.randomUUID(),
            userId = userId,
            projectId = createDto.projectId,
            title = createDto.title,
            description = createDto.description,
            status = com.circadia.reminders.domain.TaskStatus.PAUSED,
            dueDate = createDto.dueDate,
            contextTags = createDto.contextTags.toTypedArray(),
            priority = createDto.priority,
            isRecurring = true,
            completedAt = null,
            deletedAt = null,
            version = 0,
            createdAt = now,
            updatedAt = now,
            reminderType = com.circadia.reminders.domain.ReminderType.DYNAMIC,
            executionTime = dynamicRecurrence.executionTime,
            daysOfWeek = dynamicRecurrence.daysOfWeek?.toTypedArray() ?: emptyArray(),
            intervalMinutes = dynamicRecurrence.intervalMinutes,
            triggerEvent = dynamicRecurrence.triggerEvent,
            endCondition = fromEndConditionDto(createDto.endCondition)
        )
    }
    
    private fun toRecurrenceDto(entity: ReminderEntity): RecurrenceDto {
        return when (entity.reminderType) {
            com.circadia.reminders.domain.ReminderType.STATIC -> StaticRecurrenceDto(
                executionTime = entity.executionTime!!,
                daysOfWeek = entity.daysOfWeek.toList()
            )
            com.circadia.reminders.domain.ReminderType.DYNAMIC -> DynamicRecurrenceDto(
                intervalMinutes = entity.intervalMinutes,
                triggerEvent = entity.triggerEvent!!,
                executionTime = entity.executionTime,
                daysOfWeek = if (entity.daysOfWeek.isNotEmpty()) entity.daysOfWeek.toList() else null
            )
        }
    }
    
    // EndCondition conversion methods
    private fun toEndConditionDto(json: String?): EndConditionDto? {
        if (json.isNullOrBlank()) return null
        return try {
            jsonMapper.readValue(json, EndConditionDto::class.java)
        } catch (_: Exception) {
            null
        }
    }
    
    private fun fromEndConditionDto(endCondition: EndConditionDto?): String? {
        if (endCondition == null) return null
        return try {
            jsonMapper.writeValueAsString(endCondition)
        } catch (_: Exception) {
            null
        }
    }
}
