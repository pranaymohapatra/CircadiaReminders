package com.circadia.reminders.controllers

import com.circadia.reminders.domain.ReminderType
import com.circadia.reminders.domain.TaskStatus
import com.circadia.reminders.dtos.ReminderListSummaryDto
import com.circadia.reminders.dtos.ReminderDto
import com.circadia.reminders.services.ReminderSummaryService
import com.circadia.reminders.repositories.ReminderRepository
import com.circadia.reminders.dtos.mappers.EntityMapper
import io.micronaut.data.model.Pageable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.authentication.Authentication
import java.util.UUID

@Controller("/reminders")
@Secured(SecurityRule.IS_AUTHENTICATED)
class ReminderController(
    private val reminderSummaryService: ReminderSummaryService,
    private val reminderRepository: ReminderRepository,
    private val entityMapper: EntityMapper
) {

    private val logger = LoggerFactory.getLogger(ReminderController::class.java)

    @Get
    fun getReminders(
        @QueryValue(defaultValue = "0") page: Int,
        @QueryValue(defaultValue = "20") size: Int,
        authentication: Authentication
    ): ReminderListSummaryDto {
        val pageable = Pageable.from(page, size)
        val currentUserId = UUID.fromString(authentication.name)

        return reminderSummaryService.getReminderSummaries(currentUserId, pageable)
    }

    @Get("/{id}")
    fun getReminderById(@PathVariable id: UUID, authentication: Authentication): ReminderDto {
        val currentUserId = UUID.fromString(authentication.name)

        return reminderRepository.findById(id)
            .filter { it.userId == currentUserId }
            .map { entityMapper.toReminderDto(it) }
            .orElseThrow {
                HttpStatusException(HttpStatus.NOT_FOUND, "Reminder not found")
            }
    }
}
