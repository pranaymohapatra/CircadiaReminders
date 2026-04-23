package com.circadia.reminders.controllers

import com.circadia.reminders.dtos.LoginRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.authentication.Authenticator
import io.reactivex.Single
import org.reactivestreams.Publisher

@Controller("/login")
@Secured(SecurityRule.IS_ANONYMOUS)
class AuthenticationController(
    private val authenticator: Authenticator
) {

    @Post
    fun login(@Body loginRequest: LoginRequest): Publisher<AuthenticationResponse> {
        val authenticationRequest = AuthenticationRequest.build(loginRequest, "")
        return authenticator.authenticate(authenticationRequest)
    }
}
