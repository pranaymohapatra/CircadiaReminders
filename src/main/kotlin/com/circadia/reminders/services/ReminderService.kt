package com.circadia.reminders.services

import com.circadia.reminders.dtos.ReminderDto
import com.circadia.reminders.entities.ReminderEntity
import com.circadia.reminders.repositories.ReminderRepository
import com.circadia.reminders.domain.TaskStatus
import com.circadia.reminders.domain.ReminderType
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Singleton
open class ReminderService(private val reminderRepository: ReminderRepository) {

    @Transactional
    open suspend fun create(userId: UUID, dto: ReminderDto): ReminderEntity {
        val entity = ReminderEntity(
            id = UUID.randomUUID(),
            userId = userId,
            projectId = dto.projectId,
            title = dto.title,
            description = dto.description,
            status = dto.status,
            dueDate = dto.dueDate,
            contextTags = dto.contextTags.toTypedArray(),
            priority = dto.priority,
            isRecurring = dto.isRecurring,
            completedAt = dto.completedAt,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            reminderType = if (dto.isRecurring) ReminderType.DYNAMIC else ReminderType.STATIC,
            executionTime = null, // Logic to be refined based on recurrenceDto
            daysOfWeek = emptyArray(),
            intervalMinutes = null,
            triggerEvent = null
        )
        return reminderRepository.save(entity)
    }

    @Transactional
    open suspend fun update(id: UUID, dto: ReminderDto): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        val updated = existing.copy(
            title = dto.title,
            status = dto.status,
            updatedAt = OffsetDateTime.now()
        )
        return reminderRepository.update(updated)
    }

    @Transactional
    open suspend fun pause(id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        return reminderRepository.update(existing.copy(status = TaskStatus.PAUSED))
    }

    @Transactional
    open suspend fun resume(id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        return reminderRepository.update(existing.copy(status = TaskStatus.ACTIVE))
    }
}
