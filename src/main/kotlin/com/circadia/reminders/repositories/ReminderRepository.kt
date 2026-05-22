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
    @Query("SELECT * FROM reminders WHERE user_id = :userId AND deleted_at IS NULL ORDER BY updated_at DESC :pageable")
    suspend fun findByUserId(userId: UUID, pageable: Pageable): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE user_id = :userId AND status = :status AND deleted_at IS NULL ORDER BY updated_at DESC :pageable")
    suspend fun findByUserIdAndStatus(userId: UUID, status: TaskStatus, pageable: Pageable): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE user_id = :userId AND project_id = :projectId AND deleted_at IS NULL ORDER BY updated_at DESC :pageable")
    suspend fun findByUserIdAndProjectId(userId: UUID, projectId: UUID, pageable: Pageable): List<ReminderEntity>

    // 2. Fixed Summary Query
    // We use @Query so Micronaut stops trying to guess what "Summary" means.
    // This fetches only the columns needed for your Projection.
    @Query(
        """
        SELECT id, title, reminder_type, execution_time, days_of_week, interval_minutes, trigger_event, end_condition
        FROM reminders
        WHERE user_id = :userId AND deleted_at IS NULL
        ORDER BY updated_at DESC
        :pageable
        """
    )
    suspend fun findSummaryByUserId(userId: UUID, pageable: Pageable): List<ReminderSummaryProjection>

    // 3. Counts
    @Query("SELECT COUNT(*) FROM reminders WHERE user_id = :userId AND deleted_at IS NULL")
    suspend fun countByUserId(userId: UUID): Long

    @Query("SELECT COUNT(*) FROM reminders WHERE user_id = :userId AND status = :status AND deleted_at IS NULL")
    suspend fun countByUserIdAndStatus(userId: UUID, status: TaskStatus): Long

    // 4. Soft deletions
    @Query("UPDATE reminders SET deleted_at = :now, updated_at = :now WHERE user_id = :userId AND id = :id AND deleted_at IS NULL")
    suspend fun softDeleteByUserIdAndId(userId: UUID, id: UUID, now: OffsetDateTime): Int

    @Query("UPDATE reminders SET deleted_at = :now, updated_at = :now WHERE user_id = :userId AND project_id = :projectId AND deleted_at IS NULL")
    suspend fun softDeleteByUserIdAndProjectId(userId: UUID, projectId: UUID, now: OffsetDateTime): Int

    // 5. Updates
    suspend fun updateStatusByUserIdAndId(userId: UUID, id: UUID, status: TaskStatus): Int

    suspend fun updateCompletedAtByUserIdAndId(userId: UUID, id: UUID, completedAt: OffsetDateTime): Int

    // 6. Sync primitives
    @Query(
        """
        SELECT * FROM reminders
        WHERE user_id = :userId AND deleted_at IS NULL AND updated_at > :since
        ORDER BY updated_at ASC
        """
    )
    suspend fun findUpdatedSince(userId: UUID, since: OffsetDateTime): List<ReminderEntity>

    @Query(
        """
        SELECT id FROM reminders
        WHERE user_id = :userId AND deleted_at IS NOT NULL AND deleted_at > :since
        ORDER BY deleted_at ASC
        """
    )
    suspend fun findDeletedIdsSince(userId: UUID, since: OffsetDateTime): List<UUID>
}
