package com.example.noxis.kernel

sealed class NoxisTool(val name: String, val description: String) {

  data class Torch(val enable: Boolean? = null) : NoxisTool(
    name = "torch_control",
    description = "Toggle or set phone camera flashlight on/off"
  )

  enum class VolumeAction { SET, MUTE, MAX, UP, DOWN }

  data class Volume(
    val action: VolumeAction = VolumeAction.SET,
    val levelPercent: Int? = null
  ) : NoxisTool(
    name = "volume_control",
    description = "Adjust, mute, or maximize media audio volume"
  )

  object BatteryStatus : NoxisTool(
    name = "battery_telemetry",
    description = "Read battery percentage, charging state, and thermal health"
  )

  object SystemDiagnostics : NoxisTool(
    name = "system_diagnostics",
    description = "Full system scan: memory, battery, connectivity, and status"
  )

  data class LaunchApp(val app: SupportedApp) : NoxisTool(
    name = "launch_application",
    description = "Launch camera, browser, maps, youtube, settings, or calculator"
  )

  enum class ClipAction { COPY, READ }

  data class Clipboard(
    val action: ClipAction = ClipAction.COPY,
    val content: String? = null
  ) : NoxisTool(
    name = "clipboard_manager",
    description = "Read or write to the device system clipboard"
  )

  data class HapticPulse(val longPulse: Boolean = false) : NoxisTool(
    name = "haptic_pulse",
    description = "Trigger device vibration and tactile feedback"
  )

  data class TimerAlarm(val minutes: Int = 5, val label: String = "Jarvis") : NoxisTool(
    name = "timer_alarm",
    description = "Set countdown timer or alarm on phone clock"
  )

  data class WebSearch(val query: String) : NoxisTool(
    name = "web_search",
    description = "Open browser with search query"
  )
}

data class ToolExecutionResult(
  val toolName: String,
  val isSuccess: Boolean,
  val outputMessage: String,
  val speechResponse: String,
  val latencyMs: Long,
  val metadata: Map<String, String> = emptyMap()
)
