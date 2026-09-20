package com.example.noxis.kernel

import com.example.noxis.data.AuditLogDao
import com.example.noxis.data.AuditLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToolExecutor(
  private val deviceController: DeviceController,
  private val auditLogDao: AuditLogDao,
  val securityGate: SecurityGate = SecurityGate()
) {

  suspend fun execute(tool: NoxisTool, originalCommand: String = ""): ToolExecutionResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    val result = try {
      when (tool) {
        is NoxisTool.Torch -> {
          val targetState = tool.enable ?: !deviceController.isFlashlightOn()
          val res = deviceController.setFlashlight(targetState)
          if (res.isSuccess) {
            val stateText = if (targetState) "illuminated" else "extinguished"
            ToolExecutionResult(
              toolName = "Torch Control",
              isSuccess = true,
              outputMessage = "Flashlight has been turned ${if (targetState) "ON" else "OFF"}.",
              speechResponse = "Torch $stateText, sir.",
              latencyMs = 0,
              metadata = mapOf("state" to targetState.toString())
            )
          } else {
            ToolExecutionResult(
              toolName = "Torch Control",
              isSuccess = false,
              outputMessage = "Flashlight control error: ${res.exceptionOrNull()?.localizedMessage ?: "Unknown hardware failure"}",
              speechResponse = "Unable to toggle torch, sir. Hardware access restricted.",
              latencyMs = 0
            )
          }
        }

        is NoxisTool.Volume -> {
          when (tool.action) {
            NoxisTool.VolumeAction.MUTE -> {
              deviceController.muteMedia()
              ToolExecutionResult(
                toolName = "Volume Control",
                isSuccess = true,
                outputMessage = "Media volume muted (0%).",
                speechResponse = "Audio muted, sir.",
                latencyMs = 0,
                metadata = mapOf("level" to "0")
              )
            }
            NoxisTool.VolumeAction.MAX -> {
              val lvl = deviceController.maxMediaVolume()
              ToolExecutionResult(
                toolName = "Volume Control",
                isSuccess = true,
                outputMessage = "Media volume set to maximum (100%).",
                speechResponse = "Volume maximized, sir.",
                latencyMs = 0,
                metadata = mapOf("level" to "$lvl")
              )
            }
            NoxisTool.VolumeAction.UP -> {
              val cur = deviceController.getMediaVolumePercent()
              val target = (cur + 15).coerceAtMost(100)
              deviceController.setMediaVolumePercent(target)
              ToolExecutionResult(
                toolName = "Volume Control",
                isSuccess = true,
                outputMessage = "Media volume increased to $target%.",
                speechResponse = "Volume adjusted to $target percent.",
                latencyMs = 0,
                metadata = mapOf("level" to "$target")
              )
            }
            NoxisTool.VolumeAction.DOWN -> {
              val cur = deviceController.getMediaVolumePercent()
              val target = (cur - 15).coerceAtLeast(0)
              deviceController.setMediaVolumePercent(target)
              ToolExecutionResult(
                toolName = "Volume Control",
                isSuccess = true,
                outputMessage = "Media volume decreased to $target%.",
                speechResponse = "Volume lowered to $target percent.",
                latencyMs = 0,
                metadata = mapOf("level" to "$target")
              )
            }
            NoxisTool.VolumeAction.SET -> {
              val target = (tool.levelPercent ?: 50).coerceIn(0, 100)
              deviceController.setMediaVolumePercent(target)
              ToolExecutionResult(
                toolName = "Volume Control",
                isSuccess = true,
                outputMessage = "Media volume calibrated to $target%.",
                speechResponse = "Volume calibrated to $target percent, sir.",
                latencyMs = 0,
                metadata = mapOf("level" to "$target")
              )
            }
          }
        }

        is NoxisTool.BatteryStatus -> {
          val bat = deviceController.getBatteryTelemetry()
          val chargeState = if (bat.isCharging) "Charging via ${bat.pluggedSource}" else "Discharging"
          val summary = "Battery: ${bat.levelPercent}% • $chargeState • Temp: ${bat.temperatureCelsius}°C • Condition: ${bat.healthStatus}"
          ToolExecutionResult(
            toolName = "Battery Telemetry",
            isSuccess = true,
            outputMessage = summary,
            speechResponse = "Battery capacity is currently at ${bat.levelPercent} percent, $chargeState.",
            latencyMs = 0,
            metadata = mapOf(
              "level" to "${bat.levelPercent}",
              "charging" to "${bat.isCharging}",
              "temp" to "${bat.temperatureCelsius}"
            )
          )
        }

        is NoxisTool.SystemDiagnostics -> {
          val bat = deviceController.getBatteryTelemetry()
          val mem = deviceController.getMemoryTelemetry()
          val net = deviceController.getNetworkTelemetry()
          val vol = deviceController.getMediaVolumePercent()
          val torch = if (deviceController.isFlashlightOn()) "Active" else "Standby"

          val summary = buildString {
            appendLine("SYSTEM TELEMETRY REPORT")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("• Power: ${bat.levelPercent}% (${if (bat.isCharging) "Charging" else "Battery"})")
            appendLine("• Memory: ${mem.usedMb}MB / ${mem.totalMb}MB (${mem.percentUsed}% load)")
            appendLine("• Network: ${net.typeName} (${if (net.isConnected) "Online" else "Offline"})")
            appendLine("• Audio Channel: $vol%")
            appendLine("• Torch Matrix: $torch")
            appendLine("• Kernel Security: ${if (securityGate.isSecurityEnforced) "Enforced" else "Standard"}")
          }

          ToolExecutionResult(
            toolName = "System Diagnostics",
            isSuccess = true,
            outputMessage = summary,
            speechResponse = "All primary phone subsystems operational. Battery at ${bat.levelPercent} percent, memory load is ${mem.percentUsed} percent.",
            latencyMs = 0
          )
        }

        is NoxisTool.LaunchApp -> {
          val res = deviceController.launchApp(tool.app)
          if (res.isSuccess) {
            ToolExecutionResult(
              toolName = "App Launcher",
              isSuccess = true,
              outputMessage = res.getOrThrow(),
              speechResponse = "Launching ${tool.app.displayName} now, sir.",
              latencyMs = 0,
              metadata = mapOf("app" to tool.app.displayName)
            )
          } else {
            ToolExecutionResult(
              toolName = "App Launcher",
              isSuccess = false,
              outputMessage = "Failed to launch ${tool.app.displayName}: ${res.exceptionOrNull()?.localizedMessage}",
              speechResponse = "Could not locate or launch ${tool.app.displayName}.",
              latencyMs = 0
            )
          }
        }

        is NoxisTool.Clipboard -> {
          when (tool.action) {
            NoxisTool.ClipAction.COPY -> {
              val txt = tool.content ?: ""
              val ok = deviceController.copyToClipboard(txt)
              ToolExecutionResult(
                toolName = "Clipboard",
                isSuccess = ok,
                outputMessage = if (ok) "Copied to clipboard: \"$txt\"" else "Failed to copy to clipboard",
                speechResponse = if (ok) "Text copied to clipboard, sir." else "Clipboard buffer rejected write.",
                latencyMs = 0
              )
            }
            NoxisTool.ClipAction.READ -> {
              val txt = deviceController.getClipboardText()
              if (txt != null) {
                ToolExecutionResult(
                  toolName = "Clipboard",
                  isSuccess = true,
                  outputMessage = "Clipboard content: \"$txt\"",
                  speechResponse = "Clipboard contains: $txt",
                  latencyMs = 0
                )
              } else {
                ToolExecutionResult(
                  toolName = "Clipboard",
                  isSuccess = true,
                  outputMessage = "Clipboard is currently empty.",
                  speechResponse = "The clipboard is currently empty, sir.",
                  latencyMs = 0
                )
              }
            }
          }
        }

        is NoxisTool.HapticPulse -> {
          deviceController.triggerHapticFeedback(tool.longPulse)
          ToolExecutionResult(
            toolName = "Haptic Engine",
            isSuccess = true,
            outputMessage = "Tactile haptic pulse transmitted.",
            speechResponse = "Haptic feedback triggered.",
            latencyMs = 0
          )
        }

        is NoxisTool.TimerAlarm -> {
          val res = deviceController.openTimer(tool.minutes * 60, tool.label)
          ToolExecutionResult(
            toolName = "Timer / Clock",
            isSuccess = res.isSuccess,
            outputMessage = res.getOrDefault("Timer set for ${tool.minutes} minutes"),
            speechResponse = "Timer set for ${tool.minutes} minutes, sir.",
            latencyMs = 0
          )
        }

        is NoxisTool.WebSearch -> {
          val res = deviceController.openWebSearch(tool.query)
          ToolExecutionResult(
            toolName = "Web Query",
            isSuccess = res.isSuccess,
            outputMessage = res.getOrDefault("Searching for '${tool.query}'"),
            speechResponse = "Opening search results for ${tool.query}.",
            latencyMs = 0
          )
        }
      }
    } catch (e: Exception) {
      ToolExecutionResult(
        toolName = tool.name,
        isSuccess = false,
        outputMessage = "Exception executing ${tool.name}: ${e.localizedMessage}",
        speechResponse = "An anomaly occurred during tool execution, sir.",
        latencyMs = 0
      )
    }

    val elapsed = System.currentTimeMillis() - startTime
    val finalResult = result.copy(latencyMs = elapsed)

    // Log to Room Database
    try {
      auditLogDao.insertLog(
        AuditLogEntity(
          toolName = finalResult.toolName,
          commandPrompt = originalCommand.ifBlank { tool.name },
          outputSummary = finalResult.outputMessage,
          isSuccess = finalResult.isSuccess,
          durationMs = elapsed
        )
      )
    } catch (_: Exception) {}

    finalResult
  }
}
