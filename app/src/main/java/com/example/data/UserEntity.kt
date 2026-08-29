package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["mobile"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val languageCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)
