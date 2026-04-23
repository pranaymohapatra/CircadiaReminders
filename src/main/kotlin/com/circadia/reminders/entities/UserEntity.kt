package com.circadia.reminders.entities

import com.circadia.reminders.domain.UserProvider
import io.micronaut.data.model.DataType
import io.micronaut.data.annotation.*
import java.time.OffsetDateTime
import java.util.*

@MappedEntity("users")
data class UserEntity(
    @Id
    @GeneratedValue(GeneratedValue.Type.UUID)
    @TypeDef(type = DataType.UUID)
    val id: UUID,
    
    val email: String,
    
    val username: String,
    
    val passwordHash: String?,
    
    @TypeDef(type = DataType.STRING)
    val provider: UserProvider,
    
    val firstName: String?,
    
    val lastName: String?,

    @field:DateCreated
    val createdAt: OffsetDateTime? = null,

    @field:DateUpdated
    val updatedAt: OffsetDateTime? = null
)
