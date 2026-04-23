package com.circadia.reminders.entities

import com.circadia.reminders.domain.ReminderType
import com.circadia.reminders.domain.TaskStatus
import com.circadia.reminders.domain.TriggerEvent
import com.circadia.reminders.domain.EndEvent
import io.micronaut.data.annotation.*
import io.micronaut.data.model.DataType
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

@MappedEntity("reminders")
data class ReminderEntity (
    @field:Id
    @GeneratedValue(GeneratedValue.Type.UUID)
    val id: UUID,

    val userId: UUID,

    val projectId: UUID?,

    val title: String,

    val description: String?,

    val status: TaskStatus = TaskStatus.PAUSED,

    val dueDate: OffsetDateTime?,

    @field:TypeDef(type = DataType.STRING_ARRAY)
    val contextTags: Array<String> = emptyArray(),

    val priority: Int = 0,

    val isRecurring: Boolean = false,


    val completedAt: OffsetDateTime?,

    @field:DateUpdated
    val createdAt: OffsetDateTime,

    @field:DateUpdated
    val updatedAt: OffsetDateTime,
    
    // Single flat structure for reminder types
    val reminderType: ReminderType = ReminderType.STATIC,

    val executionTime: LocalTime?, // For STATIC reminders (e.g., 14:00)

    @field:TypeDef(type = DataType.INTEGER_ARRAY)
    val daysOfWeek: Array<Int> = emptyArray(), // For STATIC reminders (0=Sunday, 1=Monday, etc.)

    val intervalMinutes: Int?, // For DYNAMIC reminders

    val triggerEvent: TriggerEvent?, // For DYNAMIC reminders

    // Flat EndCondition columns
    val endConditionType: String?, // Store the Enum name: "TIME", "EVENT", or "MANUAL"

    @field:TypeDef(type = DataType.STRING)
    val endConditionValue: String? // Store the actual time or event name as a string
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReminderEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
