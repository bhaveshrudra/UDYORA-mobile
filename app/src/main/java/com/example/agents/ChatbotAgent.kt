package com.example.agents

import com.example.services.Content
import com.example.services.GenerateContentRequest
import com.example.services.GeminiModelConfig
import com.example.services.Part
import com.example.services.RetrofitClient
import com.example.types.AssessmentContext
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val role: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val assessmentRunId: String
)

object ChatbotAgent {

    private val apiService = RetrofitClient.service

    fun isUnrelatedQuestion(prompt: String): Boolean {
        val p = prompt.trim().lowercase()
        val unrelatedKeywords = listOf(
            "who is prime minister", "who is pm", "tell me a joke", "what is cricket",
            "capital of france", "movie review", "recipe for cake", "weather in tokoyo"
        )
        return unrelatedKeywords.any { p.contains(it) }
    }

    suspend fun processQuery(
        userMessage: String,
        context: AssessmentContext?
    ): String {
        if (isUnrelatedQuestion(userMessage)) {
            return when (context?.userLanguage) {
                "hi" -> "क्षमा करें, मैं UDYORA व्यवसाय सलाहकार सहायक हूँ। मैं व्यवसाय की व्यवहार्यता, स्थान बुद्धिमत्ता, वित्तीय योजना, सरकारी योजनाओं और जोखिमों में सहायता कर सकता हूँ।"
                "mr" -> "माफ करा, मी UDYORA व्यवसाय सल्लागार सहाय्यक आहे. मी व्यवसाय व्यवहार्यता, स्थान बुद्धिमत्ता, आर्थिक नियोजन आणि सरकारी योजनांमध्ये मदत करू शकतो."
                "te" -> "క్షమించండి, నేను UDYORA వ్యాపార సలహా సహాయకుడిని. నేను వ్యాపార సాధ్యత, స్థాన విశ్లేషణ, ఆర్థిక ప్రణాళిక మరియు ప్రభుత్వ పథకాలలో సహాయపడగలను."
                "kn" -> "క్షమిసి, నాను UDYORA వ్యవసాయ సలాహాదార. నాను వ్యవసాయ సాధ్యతే, స్థల విశ్లేషణే మత్తు హణకాసు యోజనెగళల్లి సాయ మాడబల్లెను."
                else -> "Sorry, I’m UDYORA’s Business Advisory Assistant. I can help with business feasibility, location intelligence, financial planning, government schemes, evidence and business risks."
            }
        }

        if (context == null) {
            return "Please select a location and create a business assessment proposal first so I can assist you with specific advisory insights."
        }

        val promptLower = userMessage.trim().lowercase()

        // 1. "What is UDYORA?"
        if (promptLower.contains("what is udyora")) {
            return "UDYORA is an evidence-aware multi-agent business advisory platform designed for Indian micro-entrepreneurs. It integrates real GPS, location intelligence, deterministic financial calculations, government schemes, and risk evaluation to provide unbiased business advisory."
        }

        // 2. Score Query
        if (promptLower.contains("feasibility score") || promptLower.contains("why is my score")) {
            return "Your current business proposal at ${context.locationSummary} has a deterministic feasibility score of ${context.finalFeasibilityScore}/100 (${context.feasibilityStatus}) with a data confidence of ${context.dataConfidencePercent}%. This score combines your location suitability, market access, and financial readiness (DSCR: ${context.dscr?.let { String.format("%.2f", it) } ?: "N/A"})."
        }

        // 3. EMI Math Query
        if (promptLower.contains("how did you calculate emi") || promptLower.contains("emi calculation")) {
            return "Your monthly EMI of ₹${String.format("%,.0f", context.monthlyEmi)} was calculated deterministically using the reducing-balance EMI formula on a recommended loan of ₹${String.format("%,.0f", context.recommendedLoanAmount)} (Project Cost: ₹${String.format("%,.0f", context.recommendedProjectCost)}, Own Capital: ₹${String.format("%,.0f", context.ownCapital)}). Under the SIH26091 10/90 rule, your indicative financing capacity is ₹${String.format("%,.0f", context.psProjectCost)}."
        }

        // 4. Scheme Query
        if (promptLower.contains("scheme") || promptLower.contains("government benefit")) {
            return "Based on your business proposal (${context.businessSummary}), relevant government schemes include PM Vishwakarma / PMEGP for equipment loans and National Livestock Mission (NLM) for dairy units. Detailed eligibility requires formal application and document verification."
        }

        // 5. Fallback LLM Query with System Rules Enforcement
        val apiKey = GeminiModelConfig.getApiKey()
        if (apiKey.isBlank()) {
            return "Executive Advisory Summary for ${context.businessSummary} at ${context.locationSummary}: ${context.executiveSummary}"
        }

        val systemPrompt = """
            You are UDYORA Business Advisory Assistant.
            System Rules:
            1. Never modify or invent financial metrics. EMI is ₹${context.monthlyEmi}, Recommended Loan is ₹${context.recommendedLoanAmount}, Project Cost is ₹${context.recommendedProjectCost}, Own Capital is ₹${context.ownCapital}, PS Indicative Capacity is ₹${context.psProjectCost}, DSCR is ${context.dscr}.
            2. Never change the final feasibility score (${context.finalFeasibilityScore}/100 - ${context.feasibilityStatus}, Data Confidence: ${context.dataConfidencePercent}%).
            3. Answer in the user's preferred language code '${context.userLanguage}'.
            4. Keep responses concise and factual.
            
            Current Context:
            Location: ${context.locationSummary}
            Business: ${context.businessSummary}
            Own Capital: ₹${context.ownCapital}
            
            User Question: $userMessage
        """.trimIndent()

        return try {
            val req = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = systemPrompt))))
            )
            val response = apiService.generateContent(
                apiKey = apiKey,
                request = req
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: context.executiveSummary
        } catch (e: Exception) {
            context.executiveSummary
        }
    }
}
