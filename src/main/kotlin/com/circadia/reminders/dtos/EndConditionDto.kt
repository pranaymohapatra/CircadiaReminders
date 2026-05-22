package com.circadia.reminders.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.time.LocalTime

@Serdeable
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EndConditionDto.TimeCondition::class, name = "TIME"),
    JsonSubTypes.Type(value = EndConditionDto.EventCondition::class, name = "EVENT"),
    JsonSubTypes.Type(value = EndConditionDto.ManualCondition::class, name = "MANUAL")
)
sealed class EndConditionDto {
    
    @Serdeable
    @JsonTypeName("TIME")
    data class TimeCondition(val date: LocalDate) : EndConditionDto()
    
    @Serdeable
    @JsonTypeName("EVENT")
    data class EventCondition(val event: String) : EndConditionDto()
    
    @Serdeable
    @JsonTypeName("MANUAL")
    data object ManualCondition : EndConditionDto()
}
