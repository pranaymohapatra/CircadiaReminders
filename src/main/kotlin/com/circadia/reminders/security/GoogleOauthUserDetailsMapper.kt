package com.circadia.reminders.security

import com.circadia.reminders.domain.UserProvider
import com.circadia.reminders.services.UserService
import io.micronaut.core.annotation.Nullable
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.oauth2.endpoint.authorization.state.State
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Singleton
class GoogleOauthUserDetailsMapper : OauthUserDetailsMapper {
    
    @Inject
    lateinit var userService: UserService
    
    override fun createUserDetails(tokenResponse: TokenResponse): Publisher<io.micronaut.security.authentication.UserDetails> {
        return Mono.fromCallable {
            val email = tokenResponse.getClaims()?.get("email") as? String
                ?: throw AuthenticationFailed("Email not found in OAuth response")
            
            val firstName = tokenResponse.getClaims()?.get("given_name") as? String
            val lastName = tokenResponse.getClaims()?.get("family_name") as? String
            
            val username = email.substringBefore("@")
            
            val user = userService.getOrCreateUser(
                email = email,
                username = username,
                provider = UserProvider.GOOGLE,
                passwordHash = null,
                firstName = firstName,
                lastName = lastName
            )
            
            io.micronaut.security.authentication.UserDetails(
                user.id.toString(),
                emptyList()
            )
        }
    }
    
    override fun createAuthenticationResponse(tokenResponse: TokenResponse, @Nullable state: State?): Publisher<io.micronaut.security.authentication.UserDetails> {
        return createUserDetails(tokenResponse)
    }
}
