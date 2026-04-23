package com.circadia.reminders.dtos

import com.circadia.reminders.domain.TriggerEvent
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalTime

@Serdeable
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = StaticRecurrenceDto::class, name = "STATIC"),
    JsonSubTypes.Type(value = DynamicRecurrenceDto::class, name = "DYNAMIC")
)
sealed class RecurrenceDto

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StaticRecurrenceDto(
    val executionTime: LocalTime,
    val daysOfWeek: List<Int> // 0=Sunday, 1=Monday, etc.
) : RecurrenceDto()

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DynamicRecurrenceDto(
    val intervalMinutes: Int?, // Optional - can be null if only trigger event is used
    val triggerEvent: TriggerEvent,
    val executionTime: LocalTime? = null, // Optional - for time-based dynamic reminders
    val daysOfWeek: List<Int>? = null // Optional - for day-specific dynamic reminders
) : RecurrenceDto()
