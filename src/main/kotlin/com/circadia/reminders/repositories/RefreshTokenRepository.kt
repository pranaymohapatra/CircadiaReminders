package com.circadia.reminders.repositories

import com.circadia.reminders.entities.RefreshTokenEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface RefreshTokenRepository : CrudRepository<RefreshTokenEntity, Long> { // Changed UUID to Long

    /**
     * Used by CustomRefreshTokenPersistence to find a session during refresh.
     */
    fun findByRefreshToken(refreshToken: String): Optional<RefreshTokenEntity>

    /**
     * Useful for revoking all sessions for a specific user (e.g., password change).
     */
    fun deleteByUsername(username: String): Long

    /**
     * Optional: Check if a user has any active (non-revoked) sessions.
     */
    fun existsByUsernameAndRevokedFalse(username: String): Boolean
}