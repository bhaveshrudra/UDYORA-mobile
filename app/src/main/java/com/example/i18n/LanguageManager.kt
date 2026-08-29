package com.example.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow

object LanguageManager {
    val currentLanguage = MutableStateFlow(Language.ENGLISH)
    
    fun setLanguage(lang: Language) {
        currentLanguage.value = lang
    }
}

enum class Language(val code: String, val displayName: String, val ttsLocale: String) {
    ENGLISH("en", "English", "en-IN"),
    HINDI("hi", "हिंदी", "hi-IN"),
    MARATHI("mr", "मराठी", "mr-IN"),
    TELUGU("te", "తెలుగు", "te-IN"),
    KANNADA("kn", "ಕನ್ನಡ", "kn-IN");

    companion object {
        fun fromCode(code: String?): Language {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

@Composable
fun stringResourceLoc(key: String): String {
    val language by LanguageManager.currentLanguage.collectAsState()
    return translate(key, language)
}

fun translate(key: String, language: Language): String {
    val dict = mapOf(
        "choose_language" to mapOf(
            Language.ENGLISH to "Choose Your\nLanguage",
            Language.HINDI to "अपनी भाषा\nचुनें",
            Language.MARATHI to "तुमची भाषा\nनिवडा",
            Language.TELUGU to "మీ భాషను\nఎంచుకోండి",
            Language.KANNADA to "ನಿಮ್ಮ ಭಾಷೆಯನ್ನು\nಆರಿಸಿ"
        ),
        "select_language_desc" to mapOf(
            Language.ENGLISH to "Select the language you are most comfortable with",
            Language.HINDI to "वह भाषा चुनें जिसमें आप सबसे अधिक सहज हों",
            Language.MARATHI to "तुम्हाला सर्वात सोयीस्कर वाटेल ती भाषा निवडा",
            Language.TELUGU to "మీకు అత్యంత అనుకూలమైన భాషను ఎంచుకోండి",
            Language.KANNADA to "ನಿಮಗೆ ಹೆಚ್ಚು ಆರಾಮದಾಯಕವಾದ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ"
        ),
        "welcome" to mapOf(
            Language.ENGLISH to "Welcome to UDYORA",
            Language.HINDI to "UDYORA में आपका स्वागत है",
            Language.MARATHI to "UDYORA मध्ये आपले स्वागत आहे",
            Language.TELUGU to "UDYORA కి స్వాగతం",
            Language.KANNADA to "UDYORA ಗೆ ಸ್ವಾಗತ"
        ),
        "subtitle" to mapOf(
            Language.ENGLISH to "Business Advisory for Rural & Semi-Urban Entrepreneurs",
            Language.HINDI to "ग्रामीण और अर्ध-शहरी उद्यमियों के लिए व्यापार सलाहकार",
            Language.MARATHI to "ग्रामीण आणि निम-शहरी उद्योजकांसाठी व्यवसाय सल्लागार",
            Language.TELUGU to "గ్రామీణ మరియు సెమీ-అర్బన్ వ్యాపారవేత్తలకు వ్యాపార సలహా",
            Language.KANNADA to "ಗ್ರಾಮೀಣ ಮತ್ತು ಅರೆ-ನಗರ ಉದ್ಯಮಿಗಳಿಗೆ ವ್ಯಾಪಾರ ಸಲಹೆಗಾರ"
        ),
        "get_started" to mapOf(
            Language.ENGLISH to "Get Started",
            Language.HINDI to "शुरू करें",
            Language.MARATHI to "सुरू करा",
            Language.TELUGU to "ప్రారంభించండి",
            Language.KANNADA to "ಪ್ರಾರಂಭಿಸಿ"
        ),
        "select_language" to mapOf(
            Language.ENGLISH to "Select Your Language",
            Language.HINDI to "अपनी भाषा चुनें",
            Language.MARATHI to "तुमची भाषा निवडा",
            Language.TELUGU to "మీ భాషను ఎంచుకోండి",
            Language.KANNADA to "ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆರಿಸಿ"
        ),
        "login" to mapOf(
            Language.ENGLISH to "Login securely",
            Language.HINDI to "सुरक्षित रूप से लॉगिन करें",
            Language.MARATHI to "सुरक्षितपणे लॉग इन करा",
            Language.TELUGU to "సురక్షితంగా లాగిన్ చేయండి",
            Language.KANNADA to "ಸುರಕ್ಷಿತವಾಗಿ ಲಾಗಿನ್ ಮಾಡಿ"
        ),
        "email" to mapOf(
            Language.ENGLISH to "Email Address",
            Language.HINDI to "ईमेल पता",
            Language.MARATHI to "ईमेल पत्ता",
            Language.TELUGU to "ఇమెయిల్ చిరునామా",
            Language.KANNADA to "ಇಮೇಲ್ ವಿಳಾಸ"
        ),
        "continue" to mapOf(
            Language.ENGLISH to "Continue",
            Language.HINDI to "जारी रखें",
            Language.MARATHI to "पुढे जा",
            Language.TELUGU to "కొనసాగించండి",
            Language.KANNADA to "ಮುಂದುವರಿಸಿ"
        ),
        "home" to mapOf(
            Language.ENGLISH to "Home",
            Language.HINDI to "होम",
            Language.MARATHI to "मुख्यपृष्ठ",
            Language.TELUGU to "హోమ్",
            Language.KANNADA to "ಮುಖಪುಟ"
        ),
        "new_assessment" to mapOf(
            Language.ENGLISH to "New Assessment",
            Language.HINDI to "नया मूल्यांकन",
            Language.MARATHI to "नवीन मूल्यांकन",
            Language.TELUGU to "కొత్త అంచనా",
            Language.KANNADA to "ಹೊಸ ಮೌಲ್ಯಮಾಪನ"
        ),
        "tell_us_about" to mapOf(
            Language.ENGLISH to "Tell us about yourself",
            Language.HINDI to "अपने बारे में बताएं",
            Language.MARATHI to "तुमच्याबद्दल सांगा",
            Language.TELUGU to "మీ గురించి చెప్పండి",
            Language.KANNADA to "ನಿಮ್ಮ ಬಗ್ಗೆ ತಿಳಿಸಿ"
        ),
        "few_details" to mapOf(
            Language.ENGLISH to "A few basic details help UDYORA personalize your business advisory.",
            Language.HINDI to "कुछ बुनियादी विवरण UDYORA को आपकी व्यावसायिक सलाह को वैयक्तिकृत करने में मदद करते हैं।",
            Language.MARATHI to "काही मूलभूत तपशील UDYORA ला तुमचा व्यवसाय सल्ला वैयक्तिकृत करण्यात मदत करतात.",
            Language.TELUGU to "కొన్ని ప్రాథమిక వివరాలు మీ వ్యాపార సలహాను వ్యక్తిగతీకరించడానికి UDYORAకి సహాయపడతాయి.",
            Language.KANNADA to "ಕೆಲವು ಮೂಲಭೂತ ವಿವರಗಳು ನಿಮ್ಮ ವ್ಯಾಪಾರ ಸಲಹೆಯನ್ನು ವೈಯಕ್ತೀಕರಿಸಲು UDYORA ಗೆ ಸಹಾಯ ಮಾಡುತ್ತವೆ."
        ),
        "full_name" to mapOf(
            Language.ENGLISH to "Full Name *",
            Language.HINDI to "पूरा नाम *",
            Language.MARATHI to "पूर्ण नाव *",
            Language.TELUGU to "పూర్తి పేరు *",
            Language.KANNADA to "ಪೂರ್ಣ ಹೆಸರು *"
        ),
        "mobile_number" to mapOf(
            Language.ENGLISH to "Mobile Number (Optional)",
            Language.HINDI to "मोबाइल नंबर (वैकल्पिक)",
            Language.MARATHI to "मोबाईल नंबर (पर्यायी)",
            Language.TELUGU to "మొబైల్ నంబర్ (ఐచ్ఛికం)",
            Language.KANNADA to "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ (ಐಚ್ಛಿಕ)"
        ),
        "how_udyora_builds" to mapOf(
            Language.ENGLISH to "How UDYORA builds your advisory",
            Language.HINDI to "UDYORA आपकी सलाह कैसे बनाता है",
            Language.MARATHI to "UDYORA तुमचा सल्ला कसा बनवतो",
            Language.TELUGU to "UDYORA మీ సలహాను ఎలా రూపొందిస్తుంది",
            Language.KANNADA to "UDYORA ನಿಮ್ಮ ಸಲಹೆಯನ್ನು ಹೇಗೆ ನಿರ್ಮಿಸುತ್ತದೆ"
        ),
        "transparency_sub" to mapOf(
            Language.ENGLISH to "UDYORA combines your information with verified and contextual data to evaluate your business idea.",
            Language.HINDI to "UDYORA आपके व्यावसायिक विचार का मूल्यांकन करने के लिए सत्यापित और प्रासंगिक डेटा के साथ आपकी जानकारी को जोड़ता है।",
            Language.MARATHI to "UDYORA तुमच्या व्यवसायाच्या कल्पनेचे मूल्यांकन करण्यासाठी सत्यापित आणि प्रासंगिक डेटासह तुमची माहिती एकत्र करते.",
            Language.TELUGU to "మీ వ్యాపార ఆలోచనను అంచనా వేయడానికి UDYORA మీ సమాచారాన్ని ధృవీకరించబడిన మరియు సందర్భోచిత డేటాతో మిళితం చేస్తుంది.",
            Language.KANNADA to "ನಿಮ್ಮ ವ್ಯಾಪಾರ ಕಲ್ಪನೆಯನ್ನು ಮೌಲ್ಯಮಾಪನ ಮಾಡಲು UDYORA ನಿಮ್ಮ ಮಾಹಿತಿಯನ್ನು ಪರಿಶೀಲಿಸಿದ ಮತ್ತು ಸಂದರ್ಭೋಚಿತ ಡೇಟಾದೊಂದಿಗೆ ಸಂಯೋಜಿಸುತ್ತದೆ."
        ),
        "where_located" to mapOf(
            Language.ENGLISH to "Where is your business located?",
            Language.HINDI to "आपका व्यवसाय कहाँ स्थित है?",
            Language.MARATHI to "तुमचा व्यवसाय कुठे आहे?",
            Language.TELUGU to "మీ వ్యాపారం ఎక్కడ ఉంది?",
            Language.KANNADA to "ನಿಮ್ಮ ವ್ಯಾಪಾರ ಎಲ್ಲಿದೆ?"
        ),
        "what_planning" to mapOf(
            Language.ENGLISH to "What are you planning to build?",
            Language.HINDI to "आप क्या बनाने की योजना बना रहे हैं?",
            Language.MARATHI to "तुम्ही काय बनवण्याचा विचार करत आहात?",
            Language.TELUGU to "మీరు ఏమి నిర్మించాలని ప్లాన్ చేస్తున్నారు?",
            Language.KANNADA to "ನೀವು ಏನನ್ನು ನಿರ್ಮಿಸಲು ಯೋಜಿಸುತ್ತಿದ್ದೀರಿ?"
        ),
        "own_capital" to mapOf(
            Language.ENGLISH to "Available Own Capital",
            Language.HINDI to "उपलब्ध स्वयं की पूंजी",
            Language.MARATHI to "उपलब्ध स्वतःचे भांडवल",
            Language.TELUGU to "అందుబాటులో ఉన్న సొంత మూలధనం",
            Language.KANNADA to "ಲಭ್ಯವಿರುವ ಸ್ವಂತ ಬಂಡವಾಳ"
        ),
        "review_plan" to mapOf(
            Language.ENGLISH to "Your Business Plan",
            Language.HINDI to "आपकी व्यवसाय योजना",
            Language.MARATHI to "तुमची व्यवसाय योजना",
            Language.TELUGU to "మీ వ్యాపార ప్రణాళిక",
            Language.KANNADA to "ನಿಮ್ಮ ವ್ಯಾಪಾರ ಯೋಜನೆ"
        ),
        "analyze_btn" to mapOf(
            Language.ENGLISH to "ANALYZE MY BUSINESS",
            Language.HINDI to "मेरे व्यवसाय का विश्लेषण करें",
            Language.MARATHI to "माझ्या व्यवसायाचे विश्लेषण करा",
            Language.TELUGU to "నా వ్యాపారాన్ని విశ్లేషించండి",
            Language.KANNADA to "ನನ್ನ ವ್ಯಾಪಾರವನ್ನು ವಿಶ್ಲೇಷಿಸಿ"
        )
    )
    return dict[key]?.get(language) ?: dict[key]?.get(Language.ENGLISH) ?: key
}
