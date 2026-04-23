package com.circadia.reminders.dtos

import com.circadia.reminders.domain.ReminderType
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.serde.annotation.Serdeable
import java.util.*

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReminderSummaryDto(
    val id: UUID,
    val title: String,
    val type: ReminderType,
    val displayMetadata: String
)

@Serdeable
data class ReminderListSummaryDto(
    val reminders: List<ReminderSummaryDto>,
    val totalCount: Long,
    val hasMore: Boolean
)
