package com.example.noxis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noxis.brain.BrainDecision
import com.example.noxis.brain.JarvisBrain
import com.example.noxis.data.AuditLogEntity
import com.example.noxis.data.JarvisDatabase
import com.example.noxis.data.JarvisRepository
import com.example.noxis.data.RoutineEntity
import com.example.noxis.kernel.DeviceController
import com.example.noxis.kernel.NoxisTool
import com.example.noxis.kernel.SecurityGate
import com.example.noxis.kernel.ToolExecutionResult
import com.example.noxis.kernel.ToolExecutor
import com.example.noxis.voice.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class JarvisHudState {
  STANDBY,
  LISTENING,
  PROCESSING,
  EXECUTING,
  SPEAKING
}

enum class ActiveSheetDialog {
  NONE,
  DEVICE_HUB,
  ROUTINES,
  AUDIT_LOGS,
  SETTINGS,
  PIN_CONFIRM
}

data class ChatMessage(
  val id: String = System.currentTimeMillis().toString() + "_" + (1..1000).random(),
  val sender: MessageSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val toolResult: ToolExecutionResult? = null,
  val isTelemetry: Boolean = false
)

enum class MessageSender {
  USER,
  JARVIS,
  NOXIS_KERNEL
}

data class LiveDeviceStatus(
  val batteryPercent: Int = 85,
  val isCharging: Boolean = false,
  val mediaVolumePercent: Int = 60,
  val isTorchOn: Boolean = false,
  val networkInfo: String = "Online",
  val memoryUsedPercent: Int = 42,
  val isSecurityEnforced: Boolean = false
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

  private val context = application.applicationContext
  private val database = JarvisDatabase.getInstance(context)
  private val repository = JarvisRepository(database)

  val deviceController = DeviceController(context)
  private val securityGate = SecurityGate()
  val toolExecutor = ToolExecutor(deviceController, database.auditLogDao(), securityGate)
  private val brain = JarvisBrain()
  val voiceManager = VoiceManager(context)

  private val _hudState = MutableStateFlow(JarvisHudState.STANDBY)
  val hudState: StateFlow<JarvisHudState> = _hudState.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _deviceStatus = MutableStateFlow(LiveDeviceStatus())
  val deviceStatus: StateFlow<LiveDeviceStatus> = _deviceStatus.asStateFlow()

  private val _activeSheet = MutableStateFlow(ActiveSheetDialog.NONE)
  val activeSheet: StateFlow<ActiveSheetDialog> = _activeSheet.asStateFlow()

  private val _userCallsign = MutableStateFlow("Sir")
  val userCallsign: StateFlow<String> = _userCallsign.asStateFlow()

  private val _autoSpeakEnabled = MutableStateFlow(true)
  val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

  private val _pendingToolForPin = MutableStateFlow<Pair<NoxisTool, String>?>(null)
  val pendingToolForPin: StateFlow<Pair<NoxisTool, String>?> = _pendingToolForPin.asStateFlow()

  val routines: StateFlow<List<RoutineEntity>> = repository.routines
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    loadPreferences()
    refreshDeviceStatus()
    sendInitialGreeting()

    // Observe TTS speaking state
    viewModelScope.launch {
      voiceManager.isSpeaking.collect { speaking ->
        if (speaking) {
          _hudState.value = JarvisHudState.SPEAKING
        } else if (_hudState.value == JarvisHudState.SPEAKING) {
          _hudState.value = JarvisHudState.STANDBY
        }
      }
    }
  }

  private fun loadPreferences() {
    viewModelScope.launch {
      val callsign = repository.getMemory("user_callsign")
      if (!callsign.isNullOrBlank()) {
        _userCallsign.value = callsign
      }
      val autoSpeak = repository.getMemory("auto_speak")
      if (autoSpeak != null) {
        _autoSpeakEnabled.value = autoSpeak.toBoolean()
        voiceManager.isVoiceEnabled = _autoSpeakEnabled.value
      }
      val pin = repository.getMemory("security_pin")
      if (!pin.isNullOrBlank()) {
        securityGate.updatePin(pin)
      }
    }
  }

  fun refreshDeviceStatus() {
    val bat = deviceController.getBatteryTelemetry()
    val vol = deviceController.getMediaVolumePercent()
    val torch = deviceController.isFlashlightOn()
    val net = deviceController.getNetworkTelemetry()
    val mem = deviceController.getMemoryTelemetry()

    _deviceStatus.value = LiveDeviceStatus(
      batteryPercent = bat.levelPercent,
      isCharging = bat.isCharging,
      mediaVolumePercent = vol,
      isTorchOn = torch,
      networkInfo = net.typeName,
      memoryUsedPercent = mem.percentUsed,
      isSecurityEnforced = securityGate.isSecurityEnforced
    )
  }

  private fun sendInitialGreeting() {
    val bat = deviceController.getBatteryTelemetry()
    val greeting = "Good day, ${_userCallsign.value}. Jarvis online and interfacing with Noxis Phone Control Kernel. Power cells at ${bat.levelPercent}%, systems nominal."
    _messages.value = listOf(
      ChatMessage(
        sender = MessageSender.JARVIS,
        text = greeting
      )
    )
    if (_autoSpeakEnabled.value) {
      voiceManager.speak(greeting)
    }
  }

  fun onUserSpeechInput(spokenText: String) {
    if (spokenText.isNotBlank()) {
      submitCommand(spokenText)
    }
  }

  fun submitCommand(rawCommand: String) {
    val command = rawCommand.trim()
    if (command.isBlank()) return

    // 1. Post user message
    val userMsg = ChatMessage(sender = MessageSender.USER, text = command)
    _messages.value = _messages.value + userMsg

    _hudState.value = JarvisHudState.PROCESSING

    viewModelScope.launch {
      // Build conversation history for context
      val history = _messages.value.takeLast(6).map {
        (if (it.sender == MessageSender.USER) "user" else "model") to it.text
      }

      val decision = brain.processUserPrompt(command, history)

      when (decision) {
        is BrainDecision.DirectTool -> {
          handleToolExecution(decision.tool, command, decision.naturalSpeech)
        }
        is BrainDecision.ExecuteRoutine -> {
          executeRoutineByName(decision.routineName)
        }
        is BrainDecision.Hybrid -> {
          handleToolExecution(decision.tool, command, decision.explanation)
        }
        is BrainDecision.ConversationalAnswer -> {
          _hudState.value = JarvisHudState.STANDBY
          val jarvisMsg = ChatMessage(sender = MessageSender.JARVIS, text = decision.answerText)
          _messages.value = _messages.value + jarvisMsg
          if (_autoSpeakEnabled.value) {
            voiceManager.speak(decision.speechText)
          }
        }
      }
      refreshDeviceStatus()
    }
  }

  private suspend fun handleToolExecution(
    tool: NoxisTool,
    originalCommand: String,
    speechOverride: String? = null
  ) {
    if (securityGate.requiresConfirmation(tool)) {
      _pendingToolForPin.value = tool to originalCommand
      _activeSheet.value = ActiveSheetDialog.PIN_CONFIRM
      _hudState.value = JarvisHudState.STANDBY
      return
    }

    _hudState.value = JarvisHudState.EXECUTING
    val result = toolExecutor.execute(tool, originalCommand)
    _hudState.value = JarvisHudState.STANDBY

    val speech = speechOverride ?: result.speechResponse
    val jarvisMsg = ChatMessage(
      sender = MessageSender.JARVIS,
      text = speech,
      toolResult = result
    )
    _messages.value = _messages.value + jarvisMsg

    if (_autoSpeakEnabled.value && speech.isNotBlank()) {
      voiceManager.speak(speech)
    }
    refreshDeviceStatus()
  }

  fun confirmPinAndExecute(inputPin: String): Boolean {
    if (securityGate.verifyPin(inputPin)) {
      val pending = _pendingToolForPin.value
      _pendingToolForPin.value = null
      _activeSheet.value = ActiveSheetDialog.NONE

      if (pending != null) {
        viewModelScope.launch {
          handleToolExecution(pending.first, pending.second)
        }
      }
      return true
    }
    return false
  }

  fun cancelPinConfirm() {
    _pendingToolForPin.value = null
    _activeSheet.value = ActiveSheetDialog.NONE
  }

  fun executeRoutine(routine: RoutineEntity) {
    viewModelScope.launch {
      executeRoutineByName(routine.name)
    }
  }

  fun executeRoutineByName(routineName: String) {
    viewModelScope.launch {
      _hudState.value = JarvisHudState.EXECUTING

      val startMsg = ChatMessage(
        sender = MessageSender.JARVIS,
        text = "Initiating protocol: $routineName. Reconfiguring device parameters..."
      )
      _messages.value = _messages.value + startMsg
      if (_autoSpeakEnabled.value) {
        voiceManager.speak("Executing $routineName protocol, sir.")
      }

      val routine = routines.value.firstOrNull { it.name.equals(routineName, ignoreCase = true) }
      val commands = routine?.actionCommands?.split("\n", ",")?.map { it.trim() }?.filter { it.isNotBlank() }
        ?: listOf("battery status", "system diagnostics")

      for (cmd in commands) {
        delay(350)
        val toolDecision = brain.matchLocalIntent(cmd)
        if (toolDecision is BrainDecision.DirectTool) {
          val res = toolExecutor.execute(toolDecision.tool, cmd)
          _messages.value = _messages.value + ChatMessage(
            sender = MessageSender.NOXIS_KERNEL,
            text = res.outputMessage,
            toolResult = res
          )
        }
      }

      delay(300)
      _hudState.value = JarvisHudState.STANDBY
      refreshDeviceStatus()

      val endMsg = ChatMessage(
        sender = MessageSender.JARVIS,
        text = "$routineName protocol sequence completed successfully, ${_userCallsign.value}."
      )
      _messages.value = _messages.value + endMsg
      if (_autoSpeakEnabled.value) {
        voiceManager.speak("$routineName protocol complete.")
      }
    }
  }

  // Tactile Direct Controls
  fun toggleFlashlight() {
    viewModelScope.launch {
      handleToolExecution(NoxisTool.Torch(), "Toggle Flashlight")
    }
  }

  fun setVolume(percent: Int) {
    viewModelScope.launch {
      handleToolExecution(NoxisTool.Volume(NoxisTool.VolumeAction.SET, percent), "Set Volume $percent%")
    }
  }

  fun muteVolume() {
    viewModelScope.launch {
      handleToolExecution(NoxisTool.Volume(NoxisTool.VolumeAction.MUTE), "Mute Volume")
    }
  }

  fun openSheet(sheet: ActiveSheetDialog) {
    _activeSheet.value = sheet
  }

  fun closeSheet() {
    _activeSheet.value = ActiveSheetDialog.NONE
  }

  fun toggleSecurityMode() {
    securityGate.isSecurityEnforced = !securityGate.isSecurityEnforced
    refreshDeviceStatus()
    val msg = if (securityGate.isSecurityEnforced) {
      "Noxis Security Gate: ARMED. High-risk controls now require PIN confirmation."
    } else {
      "Noxis Security Gate: DISARMED. Standard access mode active."
    }
    _messages.value = _messages.value + ChatMessage(sender = MessageSender.JARVIS, text = msg)
    if (_autoSpeakEnabled.value) {
      voiceManager.speak(if (securityGate.isSecurityEnforced) "Security gate armed." else "Security gate disarmed.")
    }
  }

  fun toggleVoiceAutoSpeak() {
    val next = !_autoSpeakEnabled.value
    _autoSpeakEnabled.value = next
    voiceManager.isVoiceEnabled = next
    viewModelScope.launch {
      repository.saveMemory("auto_speak", next.toString(), "preferences")
    }
  }

  fun updateCallsign(newCallsign: String) {
    if (newCallsign.isNotBlank()) {
      _userCallsign.value = newCallsign
      viewModelScope.launch {
        repository.saveMemory("user_callsign", newCallsign, "identity")
      }
    }
  }

  fun updatePin(newPin: String) {
    if (newPin.length >= 4) {
      securityGate.updatePin(newPin)
      viewModelScope.launch {
        repository.saveMemory("security_pin", newPin, "security")
      }
    }
  }

  fun createRoutine(name: String, description: String, commands: String) {
    viewModelScope.launch {
      repository.saveRoutine(
        RoutineEntity(
          name = name,
          description = description,
          iconName = "settings_suggest",
          actionCommands = commands,
          isEnabled = true,
          isDefault = false
        )
      )
    }
  }

  fun deleteRoutine(routine: RoutineEntity) {
    viewModelScope.launch {
      repository.deleteRoutine(routine)
    }
  }

  fun clearAuditLogs() {
    viewModelScope.launch {
      repository.clearAuditLogs()
    }
  }

  fun clearMessages() {
    _messages.value = emptyList()
  }

  fun stopSpeaking() {
    voiceManager.stop()
    if (_hudState.value == JarvisHudState.SPEAKING) {
      _hudState.value = JarvisHudState.STANDBY
    }
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.shutdown()
  }
}
