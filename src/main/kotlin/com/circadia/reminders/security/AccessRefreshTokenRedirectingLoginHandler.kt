package com.circadia.reminders.security

import com.circadia.reminders.repositories.RefreshTokenRepository
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.config.SecurityConfigurationProperties
import io.micronaut.security.handlers.RedirectingLoginHandler
import io.micronaut.security.token.bearer.AccessRefreshTokenLoginHandler
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator
import io.micronaut.security.token.validator.RefreshTokenValidator
import jakarta.inject.Singleton

@Singleton
@Replaces(AccessRefreshTokenLoginHandler::class)
@Requires(property = SecurityConfigurationProperties.PREFIX + ".authentication", value = "bearer")
class AccessRefreshTokenRedirectingLoginHandler(
    accessRefreshTokenGenerator: AccessRefreshTokenGenerator,
    private val refreshTokenValidator: RefreshTokenValidator,
    private val refreshTokenRepository: RefreshTokenRepository
) : AccessRefreshTokenLoginHandler(accessRefreshTokenGenerator),
    RedirectingLoginHandler<HttpRequest<*>, MutableHttpResponse<*>> {

    override fun loginRefresh(
        authentication: Authentication,
        refreshToken: String,
        request: HttpRequest<*>?
    ): MutableHttpResponse<*> {
        refreshTokenValidator.validate(refreshToken).ifPresent { refreshTokenKey ->
            refreshTokenRepository.findByRefreshToken(refreshTokenKey).ifPresent { token ->
                refreshTokenRepository.update(token.copy(revoked = true))
            }
        }

        val accessRefreshToken = accessRefreshTokenGenerator.generate(authentication)
        return if (accessRefreshToken.isPresent) {
            HttpResponse.ok(accessRefreshToken.get())
        } else {
            HttpResponse.serverError<Any>()
        }
    }
}
