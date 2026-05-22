package com.circadia.reminders

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import java.util.UUID

private fun HttpClient.postForJson(path: String, body: Map<String, String>): JsonNode {
    return toBlocking().exchange(HttpRequest.POST(path, body), JsonNode::class.java).body()!!
}

@MicronautTest
class AuthFlowTest(
    @Client("/") private val client: HttpClient
) : StringSpec({

    val http = client.toBlocking()

    "local user can register, login, refresh, rotate refresh token, and logout" {
        val suffix = UUID.randomUUID().toString().take(8)
        val email = "auth-$suffix@example.com"
        val password = "password123"

        val registerResponse = http.exchange(
            HttpRequest.POST(
                "/api/v1/auth/register",
                mapOf(
                    "email" to email,
                    "username" to "auth-$suffix",
                    "password" to password,
                    "firstName" to "Test",
                    "lastName" to "User"
                )
            ),
            JsonNode::class.java
        )

        registerResponse.status shouldBe HttpStatus.CREATED
        registerResponse.body()!!["email"].asText() shouldBe email

        val loginTokens = client.postForJson(
            "/api/v1/auth/login",
            mapOf(
                "username" to email,
                "password" to password
            )
        )

        val accessToken = loginTokens["access_token"].asText()
        val refreshToken = loginTokens["refresh_token"].asText()
        accessToken shouldNotBe null
        refreshToken shouldNotBe null

        val protectedResponse = http.exchange(
            HttpRequest.GET<Any>("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken"),
            JsonNode::class.java
        )
        protectedResponse.status shouldBe HttpStatus.OK
        protectedResponse.body()!!["userId"].asText().isBlank() shouldBe false

        val refreshedTokens = client.postForJson(
            "/api/v1/auth/refresh",
            mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        )

        val rotatedAccessToken = refreshedTokens["access_token"].asText()
        val rotatedRefreshToken = refreshedTokens["refresh_token"].asText()
        rotatedAccessToken shouldNotBe accessToken
        rotatedRefreshToken shouldNotBe refreshToken

        shouldThrow<HttpClientResponseException> {
            client.postForJson(
                "/api/v1/auth/refresh",
                mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to refreshToken
                )
            )
        }

        val logoutResponse = http.exchange(
            HttpRequest.POST<Map<String, String>>(
                "/api/v1/auth/logout",
                mapOf("refresh_token" to rotatedRefreshToken)
            ).header(HttpHeaders.AUTHORIZATION, "Bearer $rotatedAccessToken"),
            Any::class.java
        )
        logoutResponse.status shouldBe HttpStatus.NO_CONTENT

        shouldThrow<HttpClientResponseException> {
            client.postForJson(
                "/api/v1/auth/refresh",
                mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to rotatedRefreshToken
                )
            )
        }
    }

    "local login rejects invalid password" {
        val suffix = UUID.randomUUID().toString().take(8)
        val email = "auth-bad-$suffix@example.com"

        http.exchange(
            HttpRequest.POST(
                "/api/v1/auth/register",
                mapOf(
                    "email" to email,
                    "username" to "auth-bad-$suffix",
                    "password" to "password123"
                )
            ),
            JsonNode::class.java
        )

        val exception = shouldThrow<HttpClientResponseException> {
            client.postForJson(
                "/api/v1/auth/login",
                mapOf(
                    "username" to email,
                    "password" to "wrong-password"
                )
            )
        }

        exception.status shouldBe HttpStatus.UNAUTHORIZED
    }
})
