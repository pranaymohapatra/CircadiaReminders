package com.circadia.reminders.services

import com.circadia.reminders.domain.UserProvider
import com.circadia.reminders.entities.UserEntity
import com.circadia.reminders.repositories.UserRepository
import jakarta.inject.Singleton
import java.time.OffsetDateTime
import java.util.*

@Singleton
class UserService(
    private val userRepository: UserRepository
) {
    
    fun getOrCreateUser(
        email: String,
        username: String,
        provider: UserProvider,
        passwordHash: String? = null,
        firstName: String? = null,
        lastName: String? = null
    ): UserEntity {
        return userRepository.findByEmail(email)
            .orElseGet {
                val newUser = UserEntity(
                    id = UUID.randomUUID(),
                    email = email,
                    username = username,
                    passwordHash = passwordHash,
                    provider = provider,
                    firstName = firstName,
                    lastName = lastName,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now()
                )
                userRepository.save(newUser)
            }
    }
    
    fun findByEmail(email: String): Optional<UserEntity> {
        return userRepository.findByEmail(email)
    }
    
    fun findById(id: UUID): Optional<UserEntity> {
        return userRepository.findById(id)
    }
}
