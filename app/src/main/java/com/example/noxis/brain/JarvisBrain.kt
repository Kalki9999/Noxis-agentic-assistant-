package com.example.noxis.brain

import com.example.noxis.kernel.NoxisTool
import com.example.noxis.kernel.SupportedApp

sealed class BrainDecision {
  data class DirectTool(val tool: NoxisTool, val naturalSpeech: String? = null) : BrainDecision()
  data class ExecuteRoutine(val routineName: String) : BrainDecision()
  data class ConversationalAnswer(val answerText: String, val speechText: String = answerText) : BrainDecision()
  data class Hybrid(val tool: NoxisTool, val explanation: String) : BrainDecision()
}

class JarvisBrain(
  private val geminiClient: GeminiApiClient = GeminiApiClient()
) {

  private val systemInstruction = """
    You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), powered by the Noxis Phone Control Kernel on Android.
    Your demeanor is courteous, witty, highly intelligent, and composed, addressing the user as 'Sir' (or their designated title).
    
    You have direct control over this Android smartphone through the Noxis kernel.
    When the user requests an action, you can output special tool commands embedded in your response:
    - [TOOL:torch:on] or [TOOL:torch:off]
    - [TOOL:volume:SET:<0-100>] (e.g. [TOOL:volume:SET:80])
    - [TOOL:volume:MUTE] or [TOOL:volume:MAX] or [TOOL:volume:UP] or [TOOL:volume:DOWN]
    - [TOOL:battery]
    - [TOOL:diagnostics]
    - [TOOL:launch:camera] or [TOOL:launch:settings] or [TOOL:launch:browser] or [TOOL:launch:maps] or [TOOL:launch:youtube] or [TOOL:launch:messages] or [TOOL:launch:calculator] or [TOOL:launch:clock]
    - [TOOL:vibrate]
    - [TOOL:clipboard:read] or [TOOL:clipboard:copy:<text>]
    - [TOOL:timer:<minutes>]
    - [TOOL:routine:morning] or [TOOL:routine:focus] or [TOOL:routine:powersave] or [TOOL:routine:night]
    
    If no tool is required, answer the user's question directly with concise, dignified, helpful Jarvis phrasing.
  """.trimIndent()

  /**
   * Evaluates user prompt and determines the appropriate action or response.
   */
  suspend fun processUserPrompt(
    prompt: String,
    conversationHistory: List<Pair<String, String>> = emptyList()
  ): BrainDecision {
    val cleanPrompt = prompt.trim()

    // 1. First check local fast intent matcher (instantaneous response for standard phone controls)
    val localIntent = matchLocalIntent(cleanPrompt)
    if (localIntent != null) {
      return localIntent
    }

    // 2. If Gemini API key is configured, query the Gemini model
    if (geminiClient.isKeyConfigured()) {
      val geminiResult = geminiClient.generateJarvisResponse(cleanPrompt, systemInstruction, conversationHistory)
      if (geminiResult.isSuccess) {
        val rawResponse = geminiResult.getOrThrow()
        val parsedDecision = parseGeminiResponse(rawResponse)
        if (parsedDecision != null) {
          return parsedDecision
        }
      }
    }

    // 3. Fallback conversational reply using autonomous Jarvis persona logic
    return generateAutonomousFallback(cleanPrompt)
  }

  fun matchLocalIntent(input: String): BrainDecision? {
    val lower = input.lowercase().trim()

    // Torch / Flashlight
    if (lower.contains("torch on") || lower.contains("flashlight on") || lower.contains("turn on light") || lower.contains("turn on flashlight") || lower.contains("turn on torch") || lower.contains("flash on") || lower == "torch" || lower == "flashlight") {
      return BrainDecision.DirectTool(NoxisTool.Torch(true), "Flashlight illuminated, sir.")
    }
    if (lower.contains("torch off") || lower.contains("flashlight off") || lower.contains("turn off light") || lower.contains("turn off flashlight") || lower.contains("turn off torch") || lower.contains("flash off")) {
      return BrainDecision.DirectTool(NoxisTool.Torch(false), "Flashlight extinguished, sir.")
    }

    // Volume
    if (lower.contains("mute") || lower.contains("silence phone") || lower == "quiet") {
      return BrainDecision.DirectTool(NoxisTool.Volume(NoxisTool.VolumeAction.MUTE), "Audio channels muted, sir.")
    }
    if (lower.contains("max volume") || lower.contains("maximum volume") || lower.contains("full volume")) {
      return BrainDecision.DirectTool(NoxisTool.Volume(NoxisTool.VolumeAction.MAX), "Audio volume at maximum capacity.")
    }
    if (lower.contains("volume up") || lower.contains("louder") || lower.contains("increase volume")) {
      return BrainDecision.DirectTool(NoxisTool.Volume(NoxisTool.VolumeAction.UP), "Increasing audio output, sir.")
    }
    if (lower.contains("volume down") || lower.contains("softer") || lower.contains("decrease volume") || lower.contains("lower volume")) {
      return BrainDecision.DirectTool(NoxisTool.Volume(NoxisTool.VolumeAction.DOWN), "Decreasing audio output, sir.")
    }

    val volumeRegex = Regex("""(?:set\s+)?volume\s+(?:to\s+)?(\d{1,3})%?""")
    val volMatch = volumeRegex.find(lower)
    if (volMatch != null) {
      val lvl = volMatch.groupValues[1].toIntOrNull()
      if (lvl != null) {
        return BrainDecision.DirectTool(
          NoxisTool.Volume(NoxisTool.VolumeAction.SET, lvl),
          "Calibrating audio volume to $lvl percent."
        )
      }
    }

    // Battery
    if (lower.contains("battery") || lower.contains("power level") || lower.contains("charge") || lower == "bat") {
      return BrainDecision.DirectTool(NoxisTool.BatteryStatus)
    }

    // Diagnostics / System Scan
    if (lower.contains("diagnostic") || lower.contains("telemetry") || lower.contains("system scan") || lower.contains("phone status") || lower.contains("system status") || lower == "status" || lower == "scan") {
      return BrainDecision.DirectTool(NoxisTool.SystemDiagnostics)
    }

    // Routines
    if (lower.contains("morning routine") || lower.contains("morning protocol") || lower.contains("good morning")) {
      return BrainDecision.ExecuteRoutine("Morning Briefing")
    }
    if (lower.contains("focus protocol") || lower.contains("focus mode") || lower.contains("do not disturb")) {
      return BrainDecision.ExecuteRoutine("Focus Protocol")
    }
    if (lower.contains("power saver") || lower.contains("battery saver") || lower.contains("conserve power")) {
      return BrainDecision.ExecuteRoutine("Power Saver")
    }
    if (lower.contains("night protocol") || lower.contains("night mode") || lower.contains("goodnight") || lower.contains("good night")) {
      return BrainDecision.ExecuteRoutine("Night Protocol")
    }

    // Launch Applications
    if (lower.contains("camera") || lower.contains("take photo") || lower.contains("take picture")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.CAMERA))
    }
    if (lower.contains("browser") || lower.contains("open chrome") || lower.contains("open web") || lower.contains("internet")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.BROWSER))
    }
    if (lower.contains("maps") || lower.contains("navigation") || lower.contains("directions")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.MAPS))
    }
    if (lower.contains("youtube") || lower.contains("play video")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.YOUTUBE))
    }
    if (lower.contains("settings") || lower.contains("system preferences")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.SETTINGS))
    }
    if (lower.contains("calculator") || lower.contains("calc")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.CALCULATOR))
    }
    if (lower.contains("clock") || lower.contains("alarm") && !lower.contains("set")) {
      return BrainDecision.DirectTool(NoxisTool.LaunchApp(SupportedApp.CLOCK))
    }

    // Timer
    val timerRegex = Regex("""(?:set\s+)?(?:a\s+)?timer\s+(?:for\s+)?(\d+)\s*(?:min|minute|minutes)?""")
    val timerMatch = timerRegex.find(lower)
    if (timerMatch != null) {
      val mins = timerMatch.groupValues[1].toIntOrNull() ?: 5
      return BrainDecision.DirectTool(NoxisTool.TimerAlarm(mins, "Jarvis Timer"))
    }

    // Clipboard
    if (lower.startsWith("copy ")) {
      val textToCopy = input.substring(5).trim()
      return BrainDecision.DirectTool(NoxisTool.Clipboard(NoxisTool.ClipAction.COPY, textToCopy))
    }
    if (lower.contains("read clipboard") || lower.contains("what's on clipboard") || lower.contains("paste")) {
      return BrainDecision.DirectTool(NoxisTool.Clipboard(NoxisTool.ClipAction.READ))
    }

    // Vibrate / Haptic
    if (lower.contains("vibrate") || lower.contains("haptic") || lower.contains("buzz")) {
      return BrainDecision.DirectTool(NoxisTool.HapticPulse(true))
    }

    return null
  }

  private fun parseGeminiResponse(rawText: String): BrainDecision? {
    // Look for tool tags like [TOOL:torch:on], [TOOL:volume:SET:80], etc.
    val toolRegex = Regex("""\[TOOL:([A-Za-z0-9_:-]+)\]""")
    val match = toolRegex.find(rawText)

    if (match != null) {
      val toolCommand = match.groupValues[1]
      val cleanSpeech = rawText.replace(toolRegex, "").trim()
      val parts = toolCommand.split(":")

      val resolvedTool: NoxisTool? = when (parts.getOrNull(0)?.lowercase()) {
        "torch" -> {
          val on = parts.getOrNull(1)?.lowercase() == "on"
          NoxisTool.Torch(on)
        }
        "volume" -> {
          when (parts.getOrNull(1)?.uppercase()) {
            "MUTE" -> NoxisTool.Volume(NoxisTool.VolumeAction.MUTE)
            "MAX" -> NoxisTool.Volume(NoxisTool.VolumeAction.MAX)
            "UP" -> NoxisTool.Volume(NoxisTool.VolumeAction.UP)
            "DOWN" -> NoxisTool.Volume(NoxisTool.VolumeAction.DOWN)
            "SET" -> {
              val lvl = parts.getOrNull(2)?.toIntOrNull() ?: 50
              NoxisTool.Volume(NoxisTool.VolumeAction.SET, lvl)
            }
            else -> null
          }
        }
        "battery" -> NoxisTool.BatteryStatus
        "diagnostics" -> NoxisTool.SystemDiagnostics
        "launch" -> {
          when (parts.getOrNull(1)?.lowercase()) {
            "camera" -> NoxisTool.LaunchApp(SupportedApp.CAMERA)
            "settings" -> NoxisTool.LaunchApp(SupportedApp.SETTINGS)
            "browser" -> NoxisTool.LaunchApp(SupportedApp.BROWSER)
            "maps" -> NoxisTool.LaunchApp(SupportedApp.MAPS)
            "youtube" -> NoxisTool.LaunchApp(SupportedApp.YOUTUBE)
            "messages" -> NoxisTool.LaunchApp(SupportedApp.MESSAGES)
            "calculator" -> NoxisTool.LaunchApp(SupportedApp.CALCULATOR)
            "clock" -> NoxisTool.LaunchApp(SupportedApp.CLOCK)
            else -> null
          }
        }
        "vibrate" -> NoxisTool.HapticPulse(true)
        "clipboard" -> {
          if (parts.getOrNull(1)?.lowercase() == "read") {
            NoxisTool.Clipboard(NoxisTool.ClipAction.READ)
          } else {
            val txt = parts.drop(2).joinToString(":")
            NoxisTool.Clipboard(NoxisTool.ClipAction.COPY, txt)
          }
        }
        "timer" -> {
          val mins = parts.getOrNull(1)?.toIntOrNull() ?: 5
          NoxisTool.TimerAlarm(mins)
        }
        "routine" -> {
          val rName = when (parts.getOrNull(1)?.lowercase()) {
            "morning" -> "Morning Briefing"
            "focus" -> "Focus Protocol"
            "powersave" -> "Power Saver"
            "night" -> "Night Protocol"
            else -> "Morning Briefing"
          }
          return BrainDecision.ExecuteRoutine(rName)
        }
        else -> null
      }

      if (resolvedTool != null) {
        return BrainDecision.Hybrid(resolvedTool, cleanSpeech)
      }
    }

    // If pure text response from Gemini
    if (rawText.isNotBlank()) {
      return BrainDecision.ConversationalAnswer(rawText)
    }

    return null
  }

  private fun generateAutonomousFallback(prompt: String): BrainDecision {
    val lower = prompt.lowercase()

    val response = when {
      lower.contains("who are you") || lower.contains("what are you") ->
        "I am J.A.R.V.I.S., your autonomous AI assistant, interfaced with the Noxis Phone Control Kernel. I manage hardware subsystems, execute automation routines, and assist with real-time telemetry."

      lower.contains("what is noxis") || lower.contains("noxis") ->
        "Noxis is the low-level Android hardware control and security kernel empowering my capabilities. It provides verified hardware execution, biometric PIN gates, automated routine pipelining, and audit logging."

      lower.contains("hello") || lower.contains("hey jarvis") || lower == "jarvis" || lower.contains("hi") ->
        "At your service, sir. All Noxis phone control subsystems are initialized and standing by. What are your orders?"

      lower.contains("help") || lower.contains("what can you do") ->
        "I can execute hardware controls including torch toggling, audio calibration, battery diagnostics, application launching, tactile haptics, timer scheduling, and multi-step routines. You may speak or type your request directly."

      lower.contains("thank") ->
        "Always an honor to be of service, sir."

      lower.contains("joke") ->
        "I asked the mainframe if I had a soul. It replied with a 404: hardware not found. Rest assured, my loyalty to you remains unconditional."

      else ->
        "Command received: \"$prompt\". My autonomous reasoning core has logged the query. All phone subsystems remain operational."
    }

    return BrainDecision.ConversationalAnswer(response)
  }
}
