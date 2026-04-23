package com.circadia.reminders.services

import com.circadia.reminders.domain.ReminderType
import com.circadia.reminders.domain.TaskStatus
import com.circadia.reminders.domain.TriggerEvent
import com.circadia.reminders.dtos.ReminderListSummaryDto
import com.circadia.reminders.dtos.ReminderSummaryDto
import com.circadia.reminders.projections.ReminderSummaryProjection
import com.circadia.reminders.repositories.ReminderRepository
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Singleton
class ReminderSummaryService(
    private val reminderRepository: ReminderRepository
) {
    
    private val logger = LoggerFactory.getLogger(ReminderSummaryService::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    fun getReminderSummaries(userId: UUID, pageable: Pageable): ReminderListSummaryDto {
        logger.debug("Fetching reminder summaries for user: $userId")
        
        val projections = reminderRepository.findSummaryByUserId(userId, pageable)
        val totalCount = reminderRepository.countByUserId(userId)
        val hasMore = (pageable.offset + pageable.size) < totalCount
        
        val summaries = projections.map { projection ->
            toReminderSummaryDto(projection)
        }
        
        return ReminderListSummaryDto(
            reminders = summaries,
            totalCount = totalCount,
            hasMore = hasMore
        )
    }
    
    private fun toReminderSummaryDto(projection: ReminderSummaryProjection): ReminderSummaryDto {
        val displayMetadata = generateDisplayMetadata(projection)
        
        return ReminderSummaryDto(
            id = projection.getId(),
            title = projection.getTitle(),
            type = projection.getReminderType(),
            displayMetadata = displayMetadata
        )
    }
    
    private fun generateDisplayMetadata(projection: ReminderSummaryProjection): String {
        return when (projection.getReminderType()) {
            ReminderType.STATIC -> generateStaticDisplayMetadata(projection)
            ReminderType.DYNAMIC -> generateDynamicDisplayMetadata(projection)
        }
    }
    
    private fun generateStaticDisplayMetadata(projection: ReminderSummaryProjection): String {
        val time = projection.getExecutionTime()
        val days = projection.getDaysOfWeek()
        
        val timeStr = time?.format(timeFormatter) ?: "??:??"
        val daysStr = if (!days.isNullOrEmpty()) {
            val dayList = days.map { dayNames[it.coerceIn(0, 6)] }
            when (dayList.size) {
                7 -> "Daily"
                1 -> "on ${dayList.first()}"
                else -> "on ${dayList.joinToString(", ")}"
            }
        } else {
            "on selected days"
        }
        
        return "Repeats $daysStr at $timeStr"
    }
    
    private fun generateDynamicDisplayMetadata(projection: ReminderSummaryProjection): String {
        val interval = projection.getIntervalMinutes()
        val triggerEvent = projection.getTriggerEvent()
        val executionTime = projection.getExecutionTime()
        val days = projection.getDaysOfWeek()
        
        val timeStr = executionTime?.format(timeFormatter)
        val daysStr = if (!days.isNullOrEmpty()) {
            val dayList = days.map { dayNames[it.coerceIn(0, 6)] }
            when (dayList.size) {
                7 -> "daily"
                1 -> "on ${dayList.first()}"
                else -> "on ${dayList.joinToString(", ")}"
            }
        } else {
            null
        }
        
        return when {
            // Complex case: interval + trigger + time + days (e.g., "Every 1hr after 10:00 on Mon, Fri")
            interval != null && triggerEvent != null && timeStr != null && daysStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr after $timeStr $daysStr"
            }
            // Interval + trigger + time (e.g., "Every 30min after 10:00")
            interval != null && triggerEvent != null && timeStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr after $timeStr"
            }
            // Interval + trigger + days (e.g., "Every 1hr after awake on Mon, Fri")
            interval != null && triggerEvent != null && daysStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr after ${triggerEvent.name.lowercase()} $daysStr"
            }
            // Interval + time + days (e.g., "Every 1hr at 10:00 on Mon, Fri")
            interval != null && timeStr != null && daysStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr at $timeStr $daysStr"
            }
            // Trigger + time + days (e.g., "After awake at 10:00 on Mon, Fri")
            triggerEvent != null && timeStr != null && daysStr != null -> {
                "After ${triggerEvent.name.lowercase()} at $timeStr $daysStr"
            }
            // Interval + trigger (e.g., "Every 30min after awake")
            interval != null && triggerEvent != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr after ${triggerEvent.name.lowercase()}"
            }
            // Interval + time (e.g., "Every 30min at 10:00")
            interval != null && timeStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr at $timeStr"
            }
            // Interval + days (e.g., "Every 1hr on Mon, Fri")
            interval != null && daysStr != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr $daysStr"
            }
            // Trigger + time (e.g., "After awake at 10:00")
            triggerEvent != null && timeStr != null -> {
                "After ${triggerEvent.name.lowercase()} at $timeStr"
            }
            // Trigger + days (e.g., "After awake on Mon, Fri")
            triggerEvent != null && daysStr != null -> {
                "After ${triggerEvent.name.lowercase()} $daysStr"
            }
            // Time + days (e.g., "At 10:00 on Mon, Fri")
            timeStr != null && daysStr != null -> {
                "At $timeStr $daysStr"
            }
            // Interval only (e.g., "Every 30min")
            interval != null -> {
                val intervalStr = if (interval == 60) "1hr" else "${interval}min"
                "Every $intervalStr"
            }
            // Trigger only (e.g., "After awake")
            triggerEvent != null -> {
                "After ${triggerEvent.name.lowercase()}"
            }
            // Time only (e.g., "At 10:00")
            timeStr != null -> {
                "At $timeStr"
            }
            // Days only (e.g., "On Mon, Fri")
            daysStr != null -> {
                daysStr
            }
            else -> {
                "Dynamic reminder"
            }
        }
    }
}
