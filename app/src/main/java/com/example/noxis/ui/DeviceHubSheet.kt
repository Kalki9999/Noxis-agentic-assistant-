package com.example.noxis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noxis.kernel.SupportedApp
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceHubSheet(
  deviceStatus: LiveDeviceStatus,
  onToggleTorch: () -> Unit,
  onSetVolume: (Int) -> Unit,
  onMuteVolume: () -> Unit,
  onLaunchApp: (SupportedApp) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var volumeSliderVal by remember(deviceStatus.mediaVolumePercent) {
    mutableFloatStateOf(deviceStatus.mediaVolumePercent.toFloat())
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DeepNavy
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Sheet Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "NOXIS DEVICE CONTROL HUB",
            color = CyanPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "Direct tactile hardware override matrix",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_device_hub_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Torch Control Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceElevated)
          .border(
            width = 1.dp,
            color = if (deviceStatus.isTorchOn) AccentGold.copy(alpha = 0.7f) else CyanPrimary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
          )
          .clickable { onToggleTorch() }
          .testTag("hub_toggle_torch_card")
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (deviceStatus.isTorchOn) AccentGold.copy(alpha = 0.2f) else SurfaceDark),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (deviceStatus.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = null,
                tint = if (deviceStatus.isTorchOn) AccentGold else TextSecondary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Flashlight Torch Subsystem",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = if (deviceStatus.isTorchOn) "ACTIVE // High-power LED on" else "STANDBY // LED off",
                color = if (deviceStatus.isTorchOn) AccentGold else TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (deviceStatus.isTorchOn) AccentGold else SurfaceDark)
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (deviceStatus.isTorchOn) "ON" else "OFF",
              color = if (deviceStatus.isTorchOn) Color.Black else TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Media Volume Control Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceElevated)
          .border(1.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Media Audio Channel",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          Text(
            text = "${volumeSliderVal.toInt()}%",
            color = CyanPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Slider(
          value = volumeSliderVal,
          onValueChange = { volumeSliderVal = it },
          onValueChangeFinished = { onSetVolume(volumeSliderVal.toInt()) },
          valueRange = 0f..100f,
          colors = SliderDefaults.colors(
            thumbColor = CyanPrimary,
            activeTrackColor = CyanPrimary,
            inactiveTrackColor = SurfaceDark
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("volume_slider")
        )

        // Quick volume presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(0 to "MUTE", 25 to "25%", 50 to "50%", 80 to "80%", 100 to "MAX").forEach { (lvl, lbl) ->
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceDark)
                .border(0.6.dp, CyanPrimary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                .clickable {
                  volumeSliderVal = lvl.toFloat()
                  onSetVolume(lvl)
                }
                .testTag("vol_preset_$lbl")
                .padding(vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = lbl,
                color = if (volumeSliderVal.toInt() == lvl) CyanPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Diagnostics Mini Row (Battery & Memory)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Battery Card
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(0.8.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BatteryFull, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Power Cells", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${deviceStatus.batteryPercent}%",
            color = AccentEmerald,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = if (deviceStatus.isCharging) "Charging active" else "Nominal discharge",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }

        // Memory Card
        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(0.8.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("RAM Usage", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${deviceStatus.memoryUsedPercent}%",
            color = CyanGlow,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Buffer healthy",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // App Launch Matrix
      Text(
        text = "APPLICATIONS & SHORTCUTS",
        color = CyanPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      val apps = listOf(
        SupportedApp.CAMERA to Icons.Default.CameraAlt,
        SupportedApp.BROWSER to Icons.Default.Language,
        SupportedApp.MAPS to Icons.Default.Map,
        SupportedApp.YOUTUBE to Icons.Default.PlayArrow,
        SupportedApp.SETTINGS to Icons.Default.Settings,
        SupportedApp.MESSAGES to Icons.Default.Message,
        SupportedApp.PHONE to Icons.Default.Phone,
        SupportedApp.CLOCK to Icons.Default.Schedule,
        SupportedApp.CALCULATOR to Icons.Default.Calculate
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        apps.take(3).forEach { (app, icon) ->
          AppLaunchPill(app, icon, onLaunchApp, Modifier.weight(1f))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        apps.drop(3).take(3).forEach { (app, icon) ->
          AppLaunchPill(app, icon, onLaunchApp, Modifier.weight(1f))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        apps.drop(6).take(3).forEach { (app, icon) ->
          AppLaunchPill(app, icon, onLaunchApp, Modifier.weight(1f))
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun AppLaunchPill(
  app: SupportedApp,
  icon: ImageVector,
  onLaunch: (SupportedApp) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(SurfaceDark)
      .border(0.7.dp, CyanPrimary.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
      .clickable { onLaunch(app) }
      .testTag("launch_app_${app.name.lowercase()}")
      .padding(horizontal = 8.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Icon(icon, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(15.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = app.displayName,
      color = TextPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1
    )
  }
}
