package com.example.noxis.kernel

enum class SafetyLevel {
  LOW_RISK,
  ELEVATED_RISK,
  CRITICAL_RISK
}

class SecurityGate(
  private var currentPin: String = "1234",
  var isSecurityEnforced: Boolean = false
) {

  fun verifyPin(inputPin: String): Boolean {
    return inputPin == currentPin
  }

  fun updatePin(newPin: String) {
    if (newPin.length >= 4) {
      currentPin = newPin
    }
  }

  fun assessToolRisk(tool: NoxisTool): SafetyLevel {
    return when (tool) {
      is NoxisTool.Torch -> SafetyLevel.LOW_RISK
      is NoxisTool.Volume -> SafetyLevel.LOW_RISK
      is NoxisTool.BatteryStatus -> SafetyLevel.LOW_RISK
      is NoxisTool.SystemDiagnostics -> SafetyLevel.LOW_RISK
      is NoxisTool.HapticPulse -> SafetyLevel.LOW_RISK
      is NoxisTool.Clipboard -> SafetyLevel.LOW_RISK
      is NoxisTool.LaunchApp -> SafetyLevel.ELEVATED_RISK
      is NoxisTool.TimerAlarm -> SafetyLevel.LOW_RISK
      is NoxisTool.WebSearch -> SafetyLevel.LOW_RISK
    }
  }

  fun requiresConfirmation(tool: NoxisTool): Boolean {
    if (!isSecurityEnforced) return false
    return assessToolRisk(tool) != SafetyLevel.LOW_RISK
  }
}
