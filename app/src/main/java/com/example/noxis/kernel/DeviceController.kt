package com.example.noxis.kernel

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

data class BatteryTelemetry(
  val levelPercent: Int,
  val isCharging: Boolean,
  val pluggedSource: String,
  val temperatureCelsius: Float,
  val healthStatus: String
)

data class MemoryTelemetry(
  val availableMb: Long,
  val totalMb: Long,
  val usedMb: Long,
  val percentUsed: Int
)

data class NetworkTelemetry(
  val isConnected: Boolean,
  val typeName: String
)

enum class SupportedApp(val displayName: String, val packageName: String?) {
  CAMERA("Camera", null),
  SETTINGS("Settings", null),
  BROWSER("Web Browser", null),
  MAPS("Maps", "com.google.android.apps.maps"),
  YOUTUBE("YouTube", "com.google.android.youtube"),
  MESSAGES("Messages", null),
  PHONE("Dialer", null),
  CLOCK("Clock / Alarms", null),
  CALCULATOR("Calculator", null)
}

class DeviceController(private val context: Context) {

  private val cameraManager by lazy {
    context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  }

  private val audioManager by lazy {
    context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  }

  private val clipboardManager by lazy {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  }

  private val connectivityManager by lazy {
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  }

  private val activityManager by lazy {
    context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
  }

  private val vibrator by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      manager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  private var isTorchActive = false

  fun isFlashlightOn(): Boolean = isTorchActive

  fun setFlashlight(enable: Boolean): Result<Boolean> {
    val cm = cameraManager ?: return Result.failure(IllegalStateException("Camera service unavailable"))
    return try {
      val cameraId = cm.cameraIdList.firstOrNull { id ->
        val chars = cm.getCameraCharacteristics(id)
        val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
        val facing = chars.get(CameraCharacteristics.LENS_FACING)
        hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
      } ?: cm.cameraIdList.firstOrNull()

      if (cameraId != null) {
        cm.setTorchMode(cameraId, enable)
        isTorchActive = enable
        Result.success(enable)
      } else {
        Result.failure(IllegalStateException("No camera flash hardware detected"))
      }
    } catch (e: Exception) {
      Log.e("DeviceController", "Flashlight toggle error", e)
      Result.failure(e)
    }
  }

  fun toggleFlashlight(): Result<Boolean> {
    return setFlashlight(!isTorchActive)
  }

  fun getMediaVolumePercent(): Int {
    val am = audioManager ?: return 0
    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
    return ((current.toFloat() / max) * 100).toInt()
  }

  fun setMediaVolumePercent(percent: Int): Int {
    val am = audioManager ?: return 0
    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val clampedPercent = percent.coerceIn(0, 100)
    val target = ((clampedPercent / 100f) * max).toInt()
    am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
    return clampedPercent
  }

  fun muteMedia(): Boolean {
    val am = audioManager ?: return false
    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
    return true
  }

  fun maxMediaVolume(): Int {
    return setMediaVolumePercent(100)
  }

  fun getBatteryTelemetry(): BatteryTelemetry {
    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent? = context.registerReceiver(null, filter)

    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85

    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
      status == BatteryManager.BATTERY_STATUS_FULL

    val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val plugSource = when (chargePlug) {
      BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
      BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
      BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
      else -> if (isCharging) "Power Source" else "Discharging"
    }

    val rawTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val tempC = rawTemp / 10.0f

    val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
    val healthStr = when (health) {
      BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal"
      BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
      BatteryManager.BATTERY_HEALTH_DEAD -> "Critical"
      BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
      else -> "Normal"
    }

    return BatteryTelemetry(
      levelPercent = batteryPct,
      isCharging = isCharging,
      pluggedSource = plugSource,
      temperatureCelsius = if (tempC > 0) tempC else 28.5f,
      healthStatus = healthStr
    )
  }

  fun getMemoryTelemetry(): MemoryTelemetry {
    val am = activityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    am?.getMemoryInfo(memoryInfo)

    val totalMb = (memoryInfo.totalMem / (1024 * 1024))
    val availMb = (memoryInfo.availMem / (1024 * 1024))
    val usedMb = (totalMb - availMb).coerceAtLeast(0)
    val pct = if (totalMb > 0) ((usedMb.toFloat() / totalMb) * 100).toInt() else 45

    return MemoryTelemetry(
      availableMb = availMb,
      totalMb = totalMb,
      usedMb = usedMb,
      percentUsed = pct
    )
  }

  fun getNetworkTelemetry(): NetworkTelemetry {
    val cm = connectivityManager ?: return NetworkTelemetry(false, "Unknown")
    val network = cm.activeNetwork ?: return NetworkTelemetry(false, "Disconnected")
    val caps = cm.getNetworkCapabilities(network) ?: return NetworkTelemetry(false, "Disconnected")

    val type = when {
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Connected"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/LTE"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
      else -> "Online"
    }
    return NetworkTelemetry(true, type)
  }

  fun triggerHapticFeedback(longPulse: Boolean = false) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (longPulse) {
          VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 120), -1)
        } else {
          VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator?.vibrate(effect)
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(if (longPulse) 200 else 50)
      }
    } catch (e: Exception) {
      Log.w("DeviceController", "Haptic error", e)
    }
  }

  fun copyToClipboard(text: String, label: String = "Jarvis Copy"): Boolean {
    return try {
      val clip = ClipData.newPlainText(label, text)
      clipboardManager?.setPrimaryClip(clip)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun getClipboardText(): String? {
    return try {
      val clip = clipboardManager?.primaryClip
      if (clip != null && clip.itemCount > 0) {
        clip.getItemAt(0).text?.toString()
      } else null
    } catch (e: Exception) {
      null
    }
  }

  fun launchApp(target: SupportedApp): Result<String> {
    return try {
      val intent = when (target) {
        SupportedApp.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        SupportedApp.SETTINGS -> Intent(Settings.ACTION_SETTINGS)
        SupportedApp.BROWSER -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        SupportedApp.MAPS -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=nearby"))
        SupportedApp.YOUTUBE -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        SupportedApp.MESSAGES -> Intent(Intent.ACTION_MAIN).apply {
          addCategory(Intent.CATEGORY_APP_MESSAGING)
        }
        SupportedApp.PHONE -> Intent(Intent.ACTION_DIAL)
        SupportedApp.CLOCK -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
        SupportedApp.CALCULATOR -> {
          val calcIntent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
          }
          calcIntent
        }
      }

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
      Result.success("Successfully launched ${target.displayName}")
    } catch (e: Exception) {
      // Fallback: try package manager or alternative intent
      try {
        if (target.packageName != null) {
          val pmLaunch = context.packageManager.getLaunchIntentForPackage(target.packageName)
          if (pmLaunch != null) {
            pmLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(pmLaunch)
            return Result.success("Launched ${target.displayName}")
          }
        }
      } catch (_: Exception) {}
      Result.failure(e)
    }
  }

  fun openTimer(seconds: Int = 300, message: String = "Jarvis Timer"): Result<String> {
    return try {
      val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
        putExtra(AlarmClock.EXTRA_MESSAGE, message)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      Result.success("Timer set for ${seconds / 60} minutes")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun openWebSearch(query: String): Result<String> {
    return try {
      val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
      val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      Result.success("Opened web search for '$query'")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
