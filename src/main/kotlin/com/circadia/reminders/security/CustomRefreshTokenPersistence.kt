package com.circadia.reminders.security

import com.circadia.reminders.entities.RefreshTokenEntity
import com.circadia.reminders.repositories.RefreshTokenRepository
import io.micronaut.context.annotation.Value
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent
import io.micronaut.security.token.refresh.RefreshTokenPersistence
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant

@Singleton
class CustomRefreshTokenPersistence(
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${micronaut.security.token.jwt.generator.refresh-token.expiration:2592000}")
    private val refreshTokenExpirationSeconds: Long
) : RefreshTokenPersistence {

    override fun persistToken(event: RefreshTokenGeneratedEvent) {
        val payload = event.authentication
        val refreshToken = event.refreshToken

        if (payload != null && refreshToken != null) {
            val entity = RefreshTokenEntity(
                // No need for UUID.randomUUID() if your DB handles ID generation
                username = payload.name,
                refreshToken = refreshToken,
                revoked = false,
            )
            refreshTokenRepository.save(entity)
        }
    }

    override fun getAuthentication(refreshToken: String): Publisher<Authentication> {
        return Mono.fromCallable {
            refreshTokenRepository.findByRefreshToken(refreshToken)
        }
            .subscribeOn(Schedulers.boundedElastic()) // Don't block the Netty thread
            .flatMap { tokenOptional ->
                val token = tokenOptional.orElse(null)
                if (token != null && !token.revoked && !isExpired(token)) {
                    // Reconstruct authentication with the stored UUID string
                    Mono.just(Authentication.build(token.username))
                } else {
                    if (token != null && !token.revoked && isExpired(token)) {
                        refreshTokenRepository.update(token.copy(revoked = true))
                    }
                    Mono.empty()
                }
            }
    }

    private fun isExpired(token: RefreshTokenEntity): Boolean {
        val createdAt = token.dateCreated ?: return false
        return createdAt.plusSeconds(refreshTokenExpirationSeconds).isBefore(Instant.now())
    }
}
