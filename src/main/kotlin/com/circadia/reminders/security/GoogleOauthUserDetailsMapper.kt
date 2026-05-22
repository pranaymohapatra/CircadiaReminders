package com.circadia.reminders.security

import com.circadia.reminders.domain.UserProvider
import com.circadia.reminders.services.UserService
import io.micronaut.core.annotation.Nullable
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.oauth2.endpoint.authorization.state.State
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdClaims
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Singleton
@Named("google")
class GoogleOauthUserDetailsMapper(
    private val userService: UserService
) : OpenIdAuthenticationMapper {

    override fun createAuthenticationResponse(
        providerName: String,
        tokenResponse: OpenIdTokenResponse,
        openIdClaims: OpenIdClaims,
        @Nullable state: State?
    ): Publisher<AuthenticationResponse> {
        return Mono.fromCallable {
            val email = openIdClaims.email
                ?: return@fromCallable AuthenticationResponse.failure("Email not found in Google OpenID claims")

            if (openIdClaims.isEmailVerified == false) {
                return@fromCallable AuthenticationResponse.failure("Google email is not verified")
            }

            val username = openIdClaims.preferredUsername ?: email.substringBefore("@")

            val user = userService.getOrCreateUser(
                email = email,
                username = username,
                provider = UserProvider.GOOGLE,
                passwordHash = null,
                firstName = openIdClaims.givenName,
                lastName = openIdClaims.familyName
            )

            AuthenticationResponse.success(
                user.id.toString(),
                mapOf(
                    "email" to user.email,
                    "username" to user.username,
                    "provider" to providerName
                )
            )
        }
    }
}
