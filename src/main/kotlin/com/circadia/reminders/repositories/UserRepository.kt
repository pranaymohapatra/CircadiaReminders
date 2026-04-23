package com.circadia.reminders.repositories

import com.circadia.reminders.entities.UserEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface UserRepository : CrudRepository<UserEntity, UUID> {
    
    fun findByEmail(email: String): Optional<UserEntity>
    
    fun findByUsername(username: String): Optional<UserEntity>
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun deleteByEmail(email: String): Long
    
    fun deleteByUsername(username: String): Long
}
