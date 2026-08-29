package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    init {
        ShadowLog.stream = System.out
    }

    @Test
    fun testNavigationCrash() {
        val scenario = Robolectric.buildActivity(MainActivity::class.java)
        scenario.setup()
        
        composeTestRule.mainClock.advanceTimeBy(3000L) // advance animation
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("English").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.waitForIdle()
        
        // Wait and find Text Field for Name
        composeTestRule.onAllNodes(hasSetTextAction()).get(0).performTextInput("Test User")
        composeTestRule.waitForIdle()
        
        println("=== UI TREE BEFORE CLICK ===")
        composeTestRule.onRoot().printToLog("UITREE")
        
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.waitForIdle()
        
        println("=== UI TREE AFTER CLICK ===")
        composeTestRule.onRoot().printToLog("UITREE")
    }
}
