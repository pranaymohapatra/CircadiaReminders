package com.circadia.reminders.services

import com.circadia.reminders.dtos.CreateReminderDto
import com.circadia.reminders.dtos.UpdateReminderDto
import com.circadia.reminders.dtos.mappers.EntityMapper
import com.circadia.reminders.entities.ReminderEntity
import com.circadia.reminders.errors.ConflictException
import com.circadia.reminders.repositories.ReminderRepository
import com.circadia.reminders.domain.TaskStatus
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Singleton
open class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val entityMapper: EntityMapper
) {

    open suspend fun getById(userId: UUID, id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        if (existing.userId != userId || existing.deletedAt != null) throw RuntimeException("Reminder not found")
        return existing
    }

    @Transactional
    open suspend fun create(userId: UUID, dto: CreateReminderDto): ReminderEntity {
        validateCreate(dto)
        val now = OffsetDateTime.now()
        val entity = entityMapper.toReminderEntity(dto, userId, now)
        return reminderRepository.save(entity)
    }

    @Transactional
    open suspend fun update(userId: UUID, id: UUID, dto: UpdateReminderDto): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        if (existing.userId != userId || existing.deletedAt != null) throw RuntimeException("Reminder not found")

        dto.expectedVersion?.let { expected ->
            if (existing.version != expected) throw ConflictException("Reminder has changed on another device")
        }
        validateUpdate(dto)

        val now = OffsetDateTime.now()
        val updated = entityMapper.applyReminderUpdate(existing, dto, now)
        return reminderRepository.update(updated)
    }

    @Transactional
    open suspend fun pause(userId: UUID, id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        if (existing.userId != userId || existing.deletedAt != null) throw RuntimeException("Reminder not found")
        return reminderRepository.update(existing.copy(status = TaskStatus.PAUSED, updatedAt = OffsetDateTime.now()))
    }

    @Transactional
    open suspend fun resume(userId: UUID, id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        if (existing.userId != userId || existing.deletedAt != null) throw RuntimeException("Reminder not found")
        return reminderRepository.update(existing.copy(status = TaskStatus.ACTIVE, updatedAt = OffsetDateTime.now()))
    }

    @Transactional
    open suspend fun delete(userId: UUID, id: UUID): ReminderEntity {
        val existing = reminderRepository.findById(id) ?: throw RuntimeException("Reminder not found")
        if (existing.userId != userId || existing.deletedAt != null) throw RuntimeException("Reminder not found")
        val now = OffsetDateTime.now()
        return reminderRepository.update(existing.copy(deletedAt = now, updatedAt = now))
    }

    @Transactional
    open suspend fun deleteByProject(userId: UUID, projectId: UUID): Int {
        val now = OffsetDateTime.now()
        return reminderRepository.softDeleteByUserIdAndProjectId(userId, projectId, now)
    }

    open suspend fun getChangesUpdatedSince(userId: UUID, since: OffsetDateTime): List<ReminderEntity> {
        return reminderRepository.findUpdatedSince(userId, since)
    }

    open suspend fun getChangesDeletedSince(userId: UUID, since: OffsetDateTime): List<UUID> {
        return reminderRepository.findDeletedIdsSince(userId, since)
    }

    private fun validateCreate(dto: CreateReminderDto) {
        require(dto.title.isNotBlank()) { "title must not be blank" }
        require(dto.recurrence != null) { "recurrence is required" }
        validateRecurrence(dto.recurrence)
        dto.endCondition?.let { validateEndCondition(it) }
        dto.contextTags.forEach { require(it.isNotBlank()) { "contextTags must not contain blanks" } }
    }

    private fun validateUpdate(dto: UpdateReminderDto) {
        dto.title?.let { require(it.isNotBlank()) { "title must not be blank" } }
        dto.recurrence?.let { validateRecurrence(it) }
        dto.endCondition?.let { validateEndCondition(it) }
        dto.contextTags?.forEach { require(it.isNotBlank()) { "contextTags must not contain blanks" } }
        if (dto.isRecurring != null && dto.recurrence == null) {
            throw IllegalArgumentException("isRecurring cannot be updated without recurrence")
        }
    }

    private fun validateRecurrence(recurrence: com.circadia.reminders.dtos.RecurrenceDto) {
        when (recurrence) {
            is com.circadia.reminders.dtos.StaticRecurrenceDto -> {
                require(recurrence.daysOfWeek.isNotEmpty()) { "daysOfWeek must not be empty for STATIC recurrence" }
                recurrence.daysOfWeek.forEach { require(it in 0..6) { "daysOfWeek must be between 0 and 6" } }
            }
            is com.circadia.reminders.dtos.DynamicRecurrenceDto -> {
                recurrence.intervalMinutes?.let { require(it > 0) { "intervalMinutes must be > 0" } }
                recurrence.daysOfWeek?.forEach { require(it in 0..6) { "daysOfWeek must be between 0 and 6" } }
            }
        }
    }

    private fun validateEndCondition(endCondition: com.circadia.reminders.dtos.EndConditionDto) {
        when (endCondition) {
            is com.circadia.reminders.dtos.EndConditionDto.TimeCondition -> Unit
            is com.circadia.reminders.dtos.EndConditionDto.EventCondition -> require(endCondition.event.isNotBlank()) { "event must not be blank" }
            is com.circadia.reminders.dtos.EndConditionDto.ManualCondition -> Unit
        }
    }
}
