package com.circadia.reminders.security

import com.circadia.reminders.services.UserService
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.provider.AuthenticationProvider
import jakarta.inject.Singleton
import org.springframework.security.crypto.password.PasswordEncoder

@Singleton
class AuthenticationProviderUserPassword(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider<HttpRequest<*>, String, String> {

    override fun authenticate(
        @Nullable httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<String, String>
    ): AuthenticationResponse {
        val identity = authenticationRequest.identity
        val secret = authenticationRequest.secret

        val userOptional = userService.findByEmail(identity)
        if (userOptional.isEmpty) {
            return AuthenticationResponse.failure("User not found")
        }

        val user = userOptional.get()
        val passwordHash = user.passwordHash

        if (passwordHash == null || !passwordEncoder.matches(secret, passwordHash)) {
            return AuthenticationResponse.failure("Invalid password")
        }

        // Returning the ID as the primary identity, and including the email/username in attributes
        return AuthenticationResponse.success(
            user.id.toString(),
            mapOf(
                "email" to user.email,
                "username" to user.username
            )
        )
    }
}
