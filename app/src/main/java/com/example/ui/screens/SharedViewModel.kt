package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agents.ChatbotAgent
import com.example.agents.ChatMessage
import com.example.agents.MultiAgentOrchestrator
import com.example.data.AssessmentEntity
import com.example.data.UserEntity
import com.example.i18n.LanguageManager
import com.example.services.DemoScenario
import com.example.services.FinancialCalculator
import com.example.services.FinancialScoringEngine
import com.example.services.GpsLocationService
import com.example.services.LocationHierarchyService
import com.example.services.SpeechRecognitionService
import com.example.services.SpeechSessionState
import com.example.types.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AnalysisState {
    IDLE,
    VALIDATING,
    RUNNING,
    COMPLETED,
    FAILED
}

class SharedViewModel : ViewModel() {

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _mobile = MutableStateFlow("")
    val mobile: StateFlow<String> = _mobile.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // Canonical Location State
    private val _currentLocation = MutableStateFlow(CanonicalLocation())
    val currentLocation: StateFlow<CanonicalLocation> = _currentLocation.asStateFlow()

    private val _gpsState = MutableStateFlow(GpsState.IDLE)
    val gpsState: StateFlow<GpsState> = _gpsState.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // Business & Financial State
    private val _businessTypeEnum = MutableStateFlow(BusinessTypeEnum.DAIRY)
    val businessTypeEnum: StateFlow<BusinessTypeEnum> = _businessTypeEnum.asStateFlow()

    private val _businessType = MutableStateFlow(BusinessTypeEnum.DAIRY.displayName)
    val businessType: StateFlow<String> = _businessType.asStateFlow()
    
    private val _businessCategory = MutableStateFlow("Agriculture / Dairy")
    val businessCategory: StateFlow<String> = _businessCategory.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _capital = MutableStateFlow("100000")
    val capital: StateFlow<String> = _capital.asStateFlow()

    // Business-Specific Inputs
    private val _dairyInputs = MutableStateFlow(DairyInputs())
    val dairyInputs: StateFlow<DairyInputs> = _dairyInputs.asStateFlow()

    private val _tailoringInputs = MutableStateFlow(TailoringInputs())
    val tailoringInputs: StateFlow<TailoringInputs> = _tailoringInputs.asStateFlow()

    private val _kiranaInputs = MutableStateFlow(KiranaInputs())
    val kiranaInputs: StateFlow<KiranaInputs> = _kiranaInputs.asStateFlow()

    private val _poultryInputs = MutableStateFlow(PoultryInputs())
    val poultryInputs: StateFlow<PoultryInputs> = _poultryInputs.asStateFlow()

    // Computed Financial Output State
    private val _financialOutput = MutableStateFlow(
        FinancialCalculator.calculateFinancials(
            ownCapitalInput = 100000.0,
            businessType = BusinessTypeEnum.DAIRY,
            dairyInputs = DairyInputs()
        )
    )
    val financialOutput: StateFlow<FinancialOutput> = _financialOutput.asStateFlow()

    // Single Authoritative Analysis Request & Analysis State
    private val _activeAnalysisRequest = MutableStateFlow<AnalysisRequest?>(null)
    val activeAnalysisRequest: StateFlow<AnalysisRequest?> = _activeAnalysisRequest.asStateFlow()

    private val _analysisState = MutableStateFlow(AnalysisState.IDLE)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // Voice STT State
    private val _speechState = MutableStateFlow(SpeechSessionState.IDLE)
    val speechState: StateFlow<SpeechSessionState> = _speechState.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatbotThinking = MutableStateFlow(false)
    val isChatbotThinking: StateFlow<Boolean> = _isChatbotThinking.asStateFlow()

    val orchestrator = MultiAgentOrchestrator()

    fun setCurrentUserId(id: String) { _currentUserId.value = id }
    fun updateName(newValue: String) { _name.value = newValue }
    fun updateMobile(newValue: String) { _mobile.value = newValue }
    fun updateEmail(newValue: String) { _email.value = newValue }

    fun loadUser(user: UserEntity) {
        _currentUserId.value = user.id
        _name.value = user.name
        _mobile.value = user.mobile
        _email.value = user.email ?: ""
    }

    // Hardware GPS Request
    fun requestHardwareGpsLocation(context: Context) {
        val nextRevision = System.currentTimeMillis()
        _gpsState.value = GpsState.LOCATING
        _locationError.value = null

        viewModelScope.launch {
            _gpsState.value = GpsState.RESOLVING
            when (val result = GpsLocationService.getCurrentHardwareLocation(context, nextRevision)) {
                is GpsLocationService.GpsResult.Success -> {
                    if (nextRevision >= _currentLocation.value.locationRevision) {
                        _currentLocation.value = result.location
                        _gpsState.value = GpsState.RESOLVED
                        _locationError.value = null
                    }
                }
                is GpsLocationService.GpsResult.Error -> {
                    if (nextRevision >= _currentLocation.value.locationRevision) {
                        _gpsState.value = GpsState.ERROR
                        _locationError.value = result.message
                    }
                }
            }
        }
    }

    fun setGpsState(state: GpsState) { _gpsState.value = state }

    fun useDemoLocation() {
        val nextRevision = System.currentTimeMillis()
        _currentLocation.value = CanonicalLocation(
            source = LocationSource.DEMO,
            stateId = DemoScenario.STATE_ID,
            stateName = DemoScenario.STATE_NAME,
            districtId = DemoScenario.DISTRICT_ID,
            districtName = DemoScenario.DISTRICT_NAME,
            mandalId = DemoScenario.MANDAL_ID,
            mandalName = DemoScenario.MANDAL_NAME,
            pincode = DemoScenario.PINCODE,
            locality = DemoScenario.LOCALITY,
            latitude = DemoScenario.LATITUDE,
            longitude = DemoScenario.LONGITUDE,
            accuracyMeters = 10.0f,
            timestamp = System.currentTimeMillis(),
            verificationStatus = LocationVerificationStatus.VERIFIED,
            locationRevision = nextRevision
        )
        _gpsState.value = GpsState.CONFIRMED
        _locationError.value = null
    }

    fun selectState(id: String, name: String) {
        val nextRevision = System.currentTimeMillis()
        _currentLocation.value = CanonicalLocation(
            source = LocationSource.MANUAL,
            stateId = id,
            stateName = name,
            verificationStatus = LocationVerificationStatus.UNVERIFIED,
            locationRevision = nextRevision
        )
        _gpsState.value = GpsState.IDLE
        _locationError.value = null
    }

    fun selectDistrict(id: String, name: String) {
        val nextRevision = System.currentTimeMillis()
        val curr = _currentLocation.value
        _currentLocation.value = curr.copy(
            source = LocationSource.MANUAL,
            districtId = id,
            districtName = name,
            mandalId = null,
            mandalName = null,
            pincode = null,
            verificationStatus = LocationVerificationStatus.UNVERIFIED,
            locationRevision = nextRevision
        )
        _gpsState.value = GpsState.IDLE
        _locationError.value = null
    }

    fun selectMandal(id: String, name: String, lat: Double, lng: Double, loc: String) {
        val nextRevision = System.currentTimeMillis()
        val curr = _currentLocation.value
        _currentLocation.value = curr.copy(
            source = LocationSource.MANUAL,
            mandalId = id,
            mandalName = name,
            latitude = lat,
            longitude = lng,
            locality = loc,
            pincode = null,
            verificationStatus = LocationVerificationStatus.UNVERIFIED,
            locationRevision = nextRevision
        )
        _gpsState.value = GpsState.IDLE
        _locationError.value = null
    }

    fun updatePincode(code: String) {
        val nextRevision = System.currentTimeMillis()
        val curr = _currentLocation.value
        _currentLocation.value = curr.copy(pincode = code, locationRevision = nextRevision)
        _locationError.value = null

        if (code.length == 6) {
            val resolved = LocationHierarchyService.findByPincode(code)
            if (resolved != null) {
                _currentLocation.value = curr.copy(
                    source = LocationSource.PINCODE,
                    stateId = resolved.first.stateId,
                    stateName = resolved.first.stateName,
                    districtId = resolved.second.districtId,
                    districtName = resolved.second.districtName,
                    mandalId = resolved.third.mandalId,
                    mandalName = resolved.third.mandalName,
                    pincode = code,
                    latitude = resolved.third.lat,
                    longitude = resolved.third.lng,
                    locality = resolved.third.locality,
                    verificationStatus = LocationVerificationStatus.VERIFIED
                )
            } else {
                _currentLocation.value = curr.copy(verificationStatus = LocationVerificationStatus.INCONSISTENT)
                _locationError.value = "Pincode does not match any known location."
            }
        }
    }

    fun confirmLocation() {
        _currentLocation.value = _currentLocation.value.copy(verificationStatus = LocationVerificationStatus.VERIFIED)
        _gpsState.value = GpsState.CONFIRMED
    }

    fun clearLocation() {
        _currentLocation.value = CanonicalLocation(locationRevision = System.currentTimeMillis())
        _gpsState.value = GpsState.IDLE
        _locationError.value = null
    }

    fun getLocationSnapshot(): CanonicalLocation = currentLocation.value.copy()

    // Business Type & Inputs Management
    fun selectBusinessType(type: BusinessTypeEnum) {
        _businessTypeEnum.value = type
        _businessType.value = type.displayName
        recalculateFinancials()
    }

    fun updateDescription(newValue: String) { _description.value = newValue }

    fun updateDairyInputs(inputs: DairyInputs) {
        _dairyInputs.value = inputs
        recalculateFinancials()
    }

    fun updateTailoringInputs(inputs: TailoringInputs) {
        _tailoringInputs.value = inputs
        recalculateFinancials()
    }

    fun updateKiranaInputs(inputs: KiranaInputs) {
        _kiranaInputs.value = inputs
        recalculateFinancials()
    }

    fun updatePoultryInputs(inputs: PoultryInputs) {
        _poultryInputs.value = inputs
        recalculateFinancials()
    }

    fun updateCapital(newValue: String) {
        _capital.value = newValue
        recalculateFinancials()
    }

    fun recalculateFinancials() {
        val ownCap = _capital.value.toDoubleOrNull() ?: 0.0
        _financialOutput.value = FinancialCalculator.calculateFinancials(
            ownCapitalInput = ownCap,
            businessType = _businessTypeEnum.value,
            dairyInputs = if (_businessTypeEnum.value == BusinessTypeEnum.DAIRY) _dairyInputs.value else null,
            tailoringInputs = if (_businessTypeEnum.value == BusinessTypeEnum.TAILORING) _tailoringInputs.value else null,
            kiranaInputs = if (_businessTypeEnum.value == BusinessTypeEnum.KIRANA) _kiranaInputs.value else null,
            poultryInputs = if (_businessTypeEnum.value == BusinessTypeEnum.POULTRY_AGRO) _poultryInputs.value else null
        )
    }

    fun prepareAndStartAnalysis(languageCode: String): AnalysisRequest {
        recalculateFinancials()
        val ownCap = _capital.value.toDoubleOrNull() ?: 100000.0
        val snapshot = createAssessmentInputSnapshot(languageCode)
        val request = AnalysisRequest(
            requestId = "REQ-" + snapshot.assessmentRunId,
            timestamp = System.currentTimeMillis(),
            location = snapshot.locationSnapshot,
            coordinates = if (snapshot.locationSnapshot.latitude != null && snapshot.locationSnapshot.longitude != null) {
                LatLngData(snapshot.locationSnapshot.latitude!!, snapshot.locationSnapshot.longitude!!)
            } else null,
            businessType = snapshot.businessSnapshot.type,
            businessDescription = snapshot.businessSnapshot.description,
            availableCapital = ownCap,
            languageCode = languageCode
        )
        _activeAnalysisRequest.value = request
        _analysisState.value = AnalysisState.RUNNING
        orchestrator.resetState()

        Log.d("UDYORA_ANALYSIS", "[UDYORA ASSESSMENT CREATED] requestId: ${request.requestId} | language: ${request.languageCode} | location: ${request.location.displayText} | business: ${request.businessType.displayName} | capital: ₹${request.availableCapital}")
        return request
    }

    fun createAssessmentInputSnapshot(languageCode: String): AssessmentInputSnapshot {
        val runId = "RUN-" + System.currentTimeMillis()
        val busSnapshot = BusinessSnapshot(
            type = _businessTypeEnum.value,
            description = _description.value,
            dairyInputs = if (_businessTypeEnum.value == BusinessTypeEnum.DAIRY) _dairyInputs.value else null,
            tailoringInputs = if (_businessTypeEnum.value == BusinessTypeEnum.TAILORING) _tailoringInputs.value else null,
            kiranaInputs = if (_businessTypeEnum.value == BusinessTypeEnum.KIRANA) _kiranaInputs.value else null,
            poultryInputs = if (_businessTypeEnum.value == BusinessTypeEnum.POULTRY_AGRO) _poultryInputs.value else null
        )
        return AssessmentInputSnapshot(
            assessmentRunId = runId,
            userId = _currentUserId.value ?: "usr_default",
            languageCode = languageCode,
            locationSnapshot = getLocationSnapshot(),
            businessSnapshot = busSnapshot,
            financialSnapshot = _financialOutput.value
        )
    }

    fun setAnalysisState(state: AnalysisState) {
        _analysisState.value = state
    }

    fun loadSessionFromAssessmentEntity(entity: AssessmentEntity) {
        try {
            val locObj = JSONObject(entity.locationJson)
            _currentLocation.value = CanonicalLocation(
                stateName = locObj.optString("state", "Telangana"),
                districtName = locObj.optString("district", "Rangareddy"),
                mandalName = locObj.optString("mandal", "Shamshabad"),
                pincode = locObj.optString("pincode", "501218"),
                verificationStatus = LocationVerificationStatus.VERIFIED
            )
            val bEnum = BusinessTypeEnum.fromDisplayName(entity.businessType)
            _businessTypeEnum.value = bEnum
            _businessType.value = bEnum.displayName
            _description.value = entity.description
            _capital.value = entity.availableCapital.toInt().toString()
            recalculateFinancials()
        } catch (e: Exception) {
            Log.e("SHARED_VIEW_MODEL", "Failed to load session from entity: ${e.message}")
        }
    }

    // Voice STT Methods
    fun startSpeechInput(context: Context, onTranscriptResult: (String) -> Unit) {
        val lang = LanguageManager.currentLanguage.value
        _speechError.value = null
        SpeechRecognitionService.startListening(
            context = context,
            language = lang,
            onStateChange = { state -> _speechState.value = state },
            onResult = { text ->
                _speechError.value = null
                onTranscriptResult(text)
            },
            onError = { err -> _speechError.value = err }
        )
    }

    fun stopSpeechInput() {
        SpeechRecognitionService.stopListening()
        _speechState.value = SpeechSessionState.IDLE
    }

    // Chatbot Methods
    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isChatbotThinking.value) return

        val activeRunId = orchestrator.assessmentContext.value?.runId ?: "RUN-GENERAL"
        val langCode = LanguageManager.currentLanguage.value.code

        val userMsg = ChatMessage(
            id = "MSG-" + System.currentTimeMillis(),
            role = "user",
            text = text.trim(),
            language = langCode,
            assessmentRunId = activeRunId
        )

        _chatMessages.value = _chatMessages.value + userMsg
        _isChatbotThinking.value = true

        viewModelScope.launch {
            val responseText = ChatbotAgent.processQuery(text.trim(), orchestrator.assessmentContext.value)
            val assistantMsg = ChatMessage(
                id = "MSG-" + (System.currentTimeMillis() + 1),
                role = "assistant",
                text = responseText,
                language = langCode,
                assessmentRunId = activeRunId
            )
            _chatMessages.value = _chatMessages.value + assistantMsg
            _isChatbotThinking.value = false
        }
    }

    fun updateBusinessType(newValue: String) { selectBusinessType(BusinessTypeEnum.fromDisplayName(newValue)) }
    fun updateBusinessCategory(newValue: String) { _businessCategory.value = newValue }
}
