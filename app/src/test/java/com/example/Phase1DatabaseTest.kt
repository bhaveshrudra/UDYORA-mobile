package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AssessmentEntity
import com.example.data.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Phase1DatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testUserInsertionAndRetrieval() = runBlocking {
        val user = UserEntity(
            id = "usr_9876543210",
            name = "Rudra Varma",
            mobile = "9876543210",
            email = "rudra@example.com",
            languageCode = "te"
        )
        database.userDao().insertUser(user)

        val retrieved = database.userDao().getUserById("usr_9876543210")
        assertNotNull(retrieved)
        assertEquals("Rudra Varma", retrieved?.name)
        assertEquals("9876543210", retrieved?.mobile)
        assertEquals("te", retrieved?.languageCode)
    }

    @Test
    fun testDuplicateMobileDetection() = runBlocking {
        val user1 = UserEntity(
            id = "usr_9876543210",
            name = "First User",
            mobile = "9876543210",
            email = "first@example.com",
            languageCode = "en"
        )
        database.userDao().insertUser(user1)

        val found = database.userDao().getUserByMobile("9876543210")
        assertNotNull(found)
        assertEquals("First User", found?.name)
    }

    @Test
    fun testDuplicateEmailDetection() = runBlocking {
        val user1 = UserEntity(
            id = "usr_9876543210",
            name = "User One",
            mobile = "9876543210",
            email = "shared@example.com",
            languageCode = "hi"
        )
        database.userDao().insertUser(user1)

        val found = database.userDao().getUserByEmail("SHARED@EXAMPLE.COM")
        assertNotNull(found)
        assertEquals("usr_9876543210", found?.id)
    }

    @Test
    fun testAssessmentOwnership() = runBlocking {
        val userId = "usr_9876543210"
        val assessment = AssessmentEntity(
            id = "UDYORA-101",
            userId = userId,
            date = System.currentTimeMillis(),
            locationJson = "{}",
            businessType = "Dairy Farming",
            description = "Micro Dairy Unit",
            availableCapital = 150000.0,
            feasibilityScore = 82,
            dataConfidence = "HIGH",
            recommendationsJson = "{}"
        )
        database.assessmentDao().insertAssessment(assessment)

        val latestUser = database.userDao().getUserById(userId)
        assertNull(latestUser) // User not created yet

        val user = UserEntity(id = userId, name = "Rudra", mobile = "9876543210", languageCode = "en")
        database.userDao().insertUser(user)

        val savedUser = database.userDao().getUserById(userId)
        assertNotNull(savedUser)
        assertEquals(userId, savedUser?.id)
    }
}
