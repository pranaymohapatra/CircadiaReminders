package com.circadia.reminders.repositories

import com.circadia.reminders.domain.TaskStatus
import com.circadia.reminders.entities.ReminderEntity
import com.circadia.reminders.projections.ReminderSummaryProjection
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.Pageable
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.OffsetDateTime
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface ReminderRepository : CoroutineCrudRepository<ReminderEntity, UUID> {

    suspend fun update(entity: ReminderEntity): ReminderEntity

    // 1. Standard full entity fetches
    suspend fun findByUserId(userId: UUID, pageable: Pageable): List<ReminderEntity>

    suspend fun findByUserIdAndStatus(userId: UUID, status: TaskStatus, pageable: Pageable): List<ReminderEntity>

    suspend fun findByUserIdAndProjectId(userId: UUID, projectId: UUID, pageable: Pageable): List<ReminderEntity>

    // 2. Fixed Summary Query
    // We use @Query so Micronaut stops trying to guess what "Summary" means.
    // This fetches only the columns needed for your Projection.
    @Query("SELECT id, title, status, due_date, priority FROM reminder WHERE user_id = :userId")
    suspend fun findSummaryByUserId(userId: UUID, pageable: Pageable): List<ReminderSummaryProjection>

    // 3. Counts
    suspend fun countByUserId(userId: UUID): Long

    suspend fun countByUserIdAndStatus(userId: UUID, status: TaskStatus): Long

    // 4. Deletions (Assuming your Entity property is named 'id')
    suspend fun deleteByUserIdAndId(userId: UUID, id: UUID): Long

    suspend fun deleteByUserIdAndProjectId(userId: UUID, projectId: UUID): Long

    // 5. Updates
    suspend fun updateStatusByUserIdAndId(userId: UUID, id: UUID, status: TaskStatus): Int

    suspend fun updateCompletedAtByUserIdAndId(userId: UUID, id: UUID, completedAt: OffsetDateTime): Int
}
