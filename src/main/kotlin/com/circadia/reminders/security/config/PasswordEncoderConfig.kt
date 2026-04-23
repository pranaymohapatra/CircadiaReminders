package com.circadia.reminders.security.config

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Bean
import jakarta.inject.Singleton
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Factory
class PasswordEncoderConfig {

    @Bean
    @Singleton
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
