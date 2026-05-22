package com.circadia.reminders.controllers

import com.circadia.reminders.dtos.CreateReminderDto
import com.circadia.reminders.dtos.ReminderDto
import com.circadia.reminders.dtos.ReminderListSummaryDto
import com.circadia.reminders.dtos.UpdateReminderDto
import com.circadia.reminders.services.ReminderSummaryService
import com.circadia.reminders.dtos.mappers.EntityMapper
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.authentication.Authentication
import com.circadia.reminders.services.ReminderService
import com.circadia.reminders.errors.ConflictException
import io.micronaut.http.HttpResponse
import java.util.UUID
import java.time.OffsetDateTime

@Controller("/reminders")
@Secured(SecurityRule.IS_AUTHENTICATED)
class ReminderController(
    private val reminderSummaryService: ReminderSummaryService,
    private val reminderService: ReminderService,
    private val entityMapper: EntityMapper
) {

    private val logger = LoggerFactory.getLogger(ReminderController::class.java)

    @Get
    suspend fun getReminders(
        @QueryValue(defaultValue = "0") page: Int,
        @QueryValue(defaultValue = "20") size: Int,
        authentication: Authentication
    ): ReminderListSummaryDto {
        val pageable = Pageable.from(page, size)
        val currentUserId = UUID.fromString(authentication.name)

        return reminderSummaryService.getReminderSummaries(currentUserId, pageable)
    }

    @Get("/{id}")
    suspend fun getReminderById(@PathVariable id: UUID, authentication: Authentication): ReminderDto {
        val currentUserId = UUID.fromString(authentication.name)

        return try {
            entityMapper.toReminderDto(reminderService.getById(currentUserId, id))
        } catch (e: RuntimeException) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message ?: "Reminder not found")
        }
    }

    @Post
    suspend fun createReminder(dto: CreateReminderDto, authentication: Authentication): ReminderDto {
        val currentUserId = UUID.fromString(authentication.name)
        return try {
            val created = reminderService.create(currentUserId, dto)
            entityMapper.toReminderDto(created)
        } catch (e: IllegalArgumentException) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        }
    }

    @Patch("/{id}")
    suspend fun updateReminder(@PathVariable id: UUID, dto: UpdateReminderDto, authentication: Authentication): ReminderDto {
        val currentUserId = UUID.fromString(authentication.name)
        return try {
            val updated = reminderService.update(currentUserId, id, dto)
            entityMapper.toReminderDto(updated)
        } catch (e: ConflictException) {
            throw HttpStatusException(HttpStatus.CONFLICT, e.message ?: "Conflict")
        } catch (e: RuntimeException) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message ?: "Reminder not found")
        } catch (e: IllegalArgumentException) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        }
    }

    @Delete("/{id}")
    suspend fun deleteReminder(@PathVariable id: UUID, authentication: Authentication): HttpResponse<Any> {
        val currentUserId = UUID.fromString(authentication.name)
        return try {
            reminderService.delete(currentUserId, id)
            HttpResponse.noContent()
        } catch (e: RuntimeException) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message ?: "Reminder not found")
        }
    }

    @Delete("/project/{projectId}")
    suspend fun deleteRemindersByProject(@PathVariable projectId: UUID, authentication: Authentication): HttpResponse<Any> {
        val currentUserId = UUID.fromString(authentication.name)
        reminderService.deleteByProject(currentUserId, projectId)
        return HttpResponse.noContent()
    }

    @Get("/changes")
    suspend fun getReminderChanges(
        @QueryValue since: OffsetDateTime,
        authentication: Authentication
    ): Map<String, Any> {
        val currentUserId = UUID.fromString(authentication.name)
        val updated = reminderService.getChangesUpdatedSince(currentUserId, since).map(entityMapper::toReminderDto)
        val deleted = reminderService.getChangesDeletedSince(currentUserId, since)
        return mapOf(
            "serverTime" to OffsetDateTime.now(),
            "updated" to updated,
            "deleted" to deleted
        )
    }
}
