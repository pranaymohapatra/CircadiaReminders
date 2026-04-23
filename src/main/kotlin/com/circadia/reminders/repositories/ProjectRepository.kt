package com.circadia.reminders.repositories

import com.circadia.reminders.entities.ProjectEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.*

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ProjectRepository : CrudRepository<ProjectEntity, UUID> {
    
    fun findByUserId(userId: UUID, pageable: Pageable): List<ProjectEntity>
    
    fun findByUserIdAndIsArchivedFalse(userId: UUID, pageable: Pageable): List<ProjectEntity>
    
    fun findByUserIdAndIsArchivedTrue(userId: UUID, pageable: Pageable): List<ProjectEntity>
    
    fun findByUserIdAndNameContainingIgnoreCase(userId: UUID, name: String, pageable: Pageable): List<ProjectEntity>
    
    fun countByUserId(userId: UUID): Long
    
    fun countByUserIdAndIsArchivedFalse(userId: UUID): Long
    
    fun deleteByUserIdAndId(userId: UUID, id: UUID): Long
    
    fun existsByUserIdAndId(userId: UUID, id: UUID): Boolean
}
