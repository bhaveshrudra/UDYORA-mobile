package com.example

import com.example.data.UserEntity
import com.example.i18n.Language
import com.example.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class Phase1ValidationTest {

    @Test
    fun testMobileNormalization() {
        assertEquals("9876543210", ValidationUtils.normalizeMobile("+91 9876543210"))
        assertEquals("9876543210", ValidationUtils.normalizeMobile("09876543210"))
        assertEquals("9876543210", ValidationUtils.normalizeMobile("91-98765-43210"))
        assertEquals("9876543210", ValidationUtils.normalizeMobile("9876543210"))
    }

    @Test
    fun testMobileValidation() {
        assertTrue(ValidationUtils.isValidMobile("9876543210"))
        assertTrue(ValidationUtils.isValidMobile("+91 8765432109"))
        assertTrue(ValidationUtils.isValidMobile("7654321098"))
        assertTrue(ValidationUtils.isValidMobile("6543210987"))

        assertFalse(ValidationUtils.isValidMobile("1234567890")) // Starts with 1
        assertFalse(ValidationUtils.isValidMobile("5555555555")) // Starts with 5
        assertFalse(ValidationUtils.isValidMobile("98765"))       // Too short
        assertFalse(ValidationUtils.isValidMobile(""))            // Blank
    }

    @Test
    fun testEmailNormalizationAndValidation() {
        assertEquals("user@example.com", ValidationUtils.normalizeEmail("  User@Example.COM "))
        assertNull(ValidationUtils.normalizeEmail("   "))
        assertNull(ValidationUtils.normalizeEmail(null))

        assertTrue(ValidationUtils.isValidEmail(null)) // Optional email is valid when empty
        assertTrue(ValidationUtils.isValidEmail(""))
        assertTrue(ValidationUtils.isValidEmail("  "))
    }

    @Test
    fun testNameValidation() {
        assertTrue(ValidationUtils.isValidName("Rudra Kumar"))
        assertTrue(ValidationUtils.isValidName("A B"))

        assertFalse(ValidationUtils.isValidName(""))
        assertFalse(ValidationUtils.isValidName("   "))
        assertFalse(ValidationUtils.isValidName("A")) // Less than 2 chars
    }

    @Test
    fun testLanguageCodeConversion() {
        assertEquals(Language.ENGLISH, Language.fromCode("en"))
        assertEquals(Language.HINDI, Language.fromCode("hi"))
        assertEquals(Language.MARATHI, Language.fromCode("mr"))
        assertEquals(Language.TELUGU, Language.fromCode("te"))
        assertEquals(Language.KANNADA, Language.fromCode("kn"))
        assertEquals(Language.ENGLISH, Language.fromCode("invalid_code"))
    }

    @Test
    fun testUserEntityCreation() {
        val user = UserEntity(
            id = "usr_9876543210",
            name = "Test User",
            mobile = "9876543210",
            email = "test@example.com",
            languageCode = "te",
            status = "ACTIVE"
        )
        assertEquals("usr_9876543210", user.id)
        assertEquals("Test User", user.name)
        assertEquals("9876543210", user.mobile)
        assertEquals("test@example.com", user.email)
        assertEquals("te", user.languageCode)
        assertEquals("ACTIVE", user.status)
        assertTrue(user.createdAt > 0)
    }
}
