package com.circadia.reminders.projections

import com.circadia.reminders.domain.ReminderType
import com.circadia.reminders.domain.TriggerEvent
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalTime
import java.util.*

@Serdeable
interface ReminderSummaryProjection {
    fun getId(): UUID
    fun getTitle(): String
    fun getReminderType(): ReminderType
    
    // Raw fields for display metadata generation
    fun getExecutionTime(): LocalTime?
    fun getDaysOfWeek(): Array<Int>?
    fun getIntervalMinutes(): Int?
    fun getTriggerEvent(): TriggerEvent?

    fun getEndCondition(): String?
}
