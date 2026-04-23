package com.circadia.reminders.entities

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.OffsetDateTime
import java.util.*

@MappedEntity("projects")
data class ProjectEntity (
    @Id
    @GeneratedValue(GeneratedValue.Type.UUID)
    val id: UUID? = null,

    val userId: UUID,

    val name: String,
    
    val color: String?,

    @DateCreated
    val createdAt: OffsetDateTime? = null,

    @DateUpdated
    val updatedAt: OffsetDateTime? = null
)
