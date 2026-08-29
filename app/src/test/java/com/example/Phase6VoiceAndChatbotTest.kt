package com.example

import com.example.agents.ChatbotAgent
import com.example.i18n.Language
import com.example.services.SpeechRecognitionService
import com.example.types.AdvisoryStatus
import com.example.types.AssessmentContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Phase6VoiceAndChatbotTest {

    @Test
    fun testLanguageLocaleMapping() {
        assertEquals("en-IN", SpeechRecognitionService.getLocaleForLanguage(Language.ENGLISH))
        assertEquals("hi-IN", SpeechRecognitionService.getLocaleForLanguage(Language.HINDI))
        assertEquals("mr-IN", SpeechRecognitionService.getLocaleForLanguage(Language.MARATHI))
        assertEquals("te-IN", SpeechRecognitionService.getLocaleForLanguage(Language.TELUGU))
        assertEquals("kn-IN", SpeechRecognitionService.getLocaleForLanguage(Language.KANNADA))
    }

    @Test
    fun testTranscriptDeduplication() {
        assertEquals("hello", SpeechRecognitionService.deduplicateTranscript("hello hello"))
        assertEquals("I want to start a dairy business", SpeechRecognitionService.deduplicateTranscript("I want to start a dairy business"))
        assertEquals("dairy farming business", SpeechRecognitionService.deduplicateTranscript("dairy farming farming business"))
    }

    @Test
    fun testUnrelatedQuestionFilter() {
        assertTrue(ChatbotAgent.isUnrelatedQuestion("Who is the prime minister of India?"))
        assertTrue(ChatbotAgent.isUnrelatedQuestion("Tell me a joke please"))
        assertFalse(ChatbotAgent.isUnrelatedQuestion("What is my feasibility score?"))
        assertFalse(ChatbotAgent.isUnrelatedQuestion("How did you calculate EMI?"))
    }

    @Test
    fun testUnrelatedQuestionResponse() = runBlocking {
        val context = AssessmentContext(
            runId = "RUN-101",
            userName = "Rudra",
            userLanguage = "en",
            locationSummary = "Shamshabad, Rangareddy",
            businessSummary = "Dairy Farming",
            ownCapital = 100000.0,
            projectCost = 300000.0,
            loanAmount = 200000.0,
            monthlyEmi = 4200.0,
            dscr = 1.8,
            finalFeasibilityScore = 78,
            feasibilityStatus = "HIGHLY FEASIBLE",
            advisoryStatus = AdvisoryStatus.PROCEED,
            executiveSummary = "Strong proposal"
        )

        val reply = ChatbotAgent.processQuery("Tell me a joke", context)
        assertTrue(reply.contains("UDYORA’s Business Advisory Assistant"))
    }

    @Test
    fun testJudgeQuestionUdyoraDefinition() = runBlocking {
        val reply = ChatbotAgent.processQuery("What is UDYORA?", null)
        assertTrue(reply.contains("multi-agent business advisory platform"))
    }

    @Test
    fun testFeasibilityScoreQuestionGrounded() = runBlocking {
        val context = AssessmentContext(
            runId = "RUN-101",
            userName = "Rudra",
            userLanguage = "en",
            locationSummary = "Shamshabad, Rangareddy",
            businessSummary = "Dairy Farming",
            ownCapital = 100000.0,
            projectCost = 300000.0,
            loanAmount = 200000.0,
            monthlyEmi = 4200.0,
            dscr = 1.8,
            finalFeasibilityScore = 78,
            feasibilityStatus = "HIGHLY FEASIBLE",
            advisoryStatus = AdvisoryStatus.PROCEED,
            executiveSummary = "Strong proposal"
        )

        val reply = ChatbotAgent.processQuery("Why is my feasibility score 78?", context)
        assertTrue(reply.contains("78/100"))
        assertTrue(reply.contains("HIGHLY FEASIBLE"))
        assertTrue(reply.contains("1.80"))
    }

    @Test
    fun testEmiMathExplanationGrounded() = runBlocking {
        val context = AssessmentContext(
            runId = "RUN-101",
            userName = "Rudra",
            userLanguage = "en",
            locationSummary = "Shamshabad, Rangareddy",
            businessSummary = "Dairy Farming",
            ownCapital = 100000.0,
            projectCost = 300000.0,
            loanAmount = 200000.0,
            monthlyEmi = 4200.0,
            dscr = 1.8,
            finalFeasibilityScore = 78,
            feasibilityStatus = "HIGHLY FEASIBLE",
            advisoryStatus = AdvisoryStatus.PROCEED,
            executiveSummary = "Strong proposal"
        )

        val reply = ChatbotAgent.processQuery("How did you calculate EMI?", context)
        assertTrue(reply.contains("4,200"))
        assertTrue(reply.contains("200,000"))
        assertTrue(reply.contains("reducing-balance"))
    }
}
