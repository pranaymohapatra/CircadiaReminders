package com.circadia.reminders.dtos.mappers

import com.circadia.reminders.dtos.*
import com.circadia.reminders.entities.*
import jakarta.inject.Singleton
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Singleton
class EntityMapper {
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    fun toUserDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            firstName = entity.firstName,
            lastName = entity.lastName,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    fun toUserResponseDto(entity: UserEntity): UserResponseDto {
        return UserResponseDto(
            id = entity.id,
            email = entity.email,
            username = entity.username,
            firstName = entity.firstName,
            lastName = entity.lastName,
            createdAt = entity.createdAt
        )
    }
    
    fun toProjectDto(entity: ProjectEntity): ProjectDto {
        return ProjectDto(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            description = entity.description,
            color = entity.color,
            isArchived = entity.archived,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    fun toProjectSummaryDto(entity: ProjectEntity, reminderCount: Long): ProjectSummaryDto {
        return ProjectSummaryDto(
            id = entity.id,
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
            completedAt = entity.completedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            recurrence = toRecurrenceDto(entity),
            endCondition = toEndConditionDto(entity.endConditionType, entity.endConditionValue)
        )
    }
    


    
    fun toUserEntity(createDto: CreateUserDto, passwordHash: String, now: OffsetDateTime): UserEntity {
        return UserEntity(
            id = UUID.randomUUID(),
            email = createDto.email,
            username = createDto.username,
            passwordHash = passwordHash,
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
            archived = false,
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
            isRecurring = createDto.isRecurring,
            completedAt = null,
            createdAt = now,
            updatedAt = now,
            reminderType = com.circadia.reminders.domain.ReminderType.STATIC,
            executionTime = staticRecurrence.executionTime,
            daysOfWeek = staticRecurrence.daysOfWeek.toTypedArray(),
            intervalMinutes = null,
            triggerEvent = null,
            endConditionType = null,
            endConditionValue = null
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
            isRecurring = createDto.isRecurring,
            completedAt = null,
            createdAt = now,
            updatedAt = now,
            reminderType = com.circadia.reminders.domain.ReminderType.DYNAMIC,
            executionTime = dynamicRecurrence.executionTime,
            daysOfWeek = dynamicRecurrence.daysOfWeek?.toTypedArray() ?: emptyArray(),
            intervalMinutes = dynamicRecurrence.intervalMinutes,
            triggerEvent = dynamicRecurrence.triggerEvent,
            endConditionType = null,
            endConditionValue = null
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
    private fun toEndConditionDto(type: String?, value: String?): EndConditionDto? {
        if (type == null) return null
        
        return when (type) {
            "TIME" -> {
                value?.let { 
                    try {
                        EndConditionDto.TimeCondition(LocalTime.parse(it, timeFormatter))
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            "EVENT" -> {
                value?.let { EndConditionDto.EventCondition(it) }
            }
            "MANUAL" -> {
                EndConditionDto.ManualCondition
            }
            else -> null
        }
    }
    
    private fun fromEndConditionDto(endCondition: EndConditionDto?): Pair<String?, String?> {
        return when (endCondition) {
            is EndConditionDto.TimeCondition -> {
                "TIME" to endCondition.time.format(timeFormatter)
            }
            is EndConditionDto.EventCondition -> {
                "EVENT" to endCondition.event
            }
            is EndConditionDto.ManualCondition -> {
                "MANUAL" to null
            }
            null -> {
                null to null
            }
        }
    }
}
