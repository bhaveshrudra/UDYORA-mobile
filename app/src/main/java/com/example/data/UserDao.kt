package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND email IS NOT NULL AND email != '' LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestUser(): UserEntity?

    @Query("SELECT * FROM users ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR mobile LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET languageCode = :languageCode, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLanguage(id: String, languageCode: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE users SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateUserStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}
