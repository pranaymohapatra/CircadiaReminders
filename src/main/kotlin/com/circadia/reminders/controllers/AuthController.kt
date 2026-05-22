package com.circadia.reminders.controllers

import com.circadia.reminders.dtos.CreateUserDto
import com.circadia.reminders.dtos.RefreshTokenRequest
import com.circadia.reminders.dtos.UserResponseDto
import com.circadia.reminders.dtos.mappers.EntityMapper
import com.circadia.reminders.errors.ConflictException
import com.circadia.reminders.repositories.RefreshTokenRepository
import com.circadia.reminders.services.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.token.validator.RefreshTokenValidator
import org.springframework.security.crypto.password.PasswordEncoder

@Controller("/auth")
class AuthController(
    private val userService: UserService,
    private val entityMapper: EntityMapper,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenValidator: RefreshTokenValidator,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Get("/me")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    fun me(authentication: Authentication): Map<String, String> {
        return mapOf("userId" to authentication.name)
    }

    @Post("/register")
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun register(@Body dto: CreateUserDto): HttpResponse<UserResponseDto> {
        if (dto.password.length < 8) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters")
        }

        return try {
            val user = userService.createLocalUser(
                email = dto.email.trim().lowercase(),
                username = dto.username.trim(),
                passwordHash = passwordEncoder.encode(dto.password),
                firstName = dto.firstName,
                lastName = dto.lastName
            )
            HttpResponse.created(entityMapper.toUserResponseDto(user))
        } catch (e: ConflictException) {
            throw HttpStatusException(HttpStatus.CONFLICT, e.message ?: "User already exists")
        }
    }

    @Post("/logout")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    fun logout(@Body dto: RefreshTokenRequest, authentication: Authentication): HttpResponse<Any> {
        val refreshTokenKey = refreshTokenValidator.validate(dto.refreshToken).orElse(null)
            ?: return HttpResponse.noContent()

        val token = refreshTokenRepository.findByRefreshToken(refreshTokenKey).orElse(null)
            ?: return HttpResponse.noContent()

        if (token.username != authentication.name) {
            throw HttpStatusException(HttpStatus.FORBIDDEN, "Refresh token does not belong to current user")
        }

        refreshTokenRepository.update(token.copy(revoked = true))
        return HttpResponse.noContent()
    }
}
