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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCrimson
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HudTopBar(
  deviceStatus: LiveDeviceStatus,
  userCallsign: String,
  autoSpeak: Boolean,
  onToggleAutoSpeak: () -> Unit,
  onOpenDeviceHub: () -> Unit,
  onOpenRoutines: () -> Unit,
  onOpenAuditLogs: () -> Unit,
  onOpenSettings: () -> Unit,
  onToggleSecurity: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SurfaceDark.copy(alpha = 0.95f))
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Top Line: Identity & Quick Subsystem Badges
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Identity
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(CyanPrimary)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "JARVIS",
          color = CyanPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "• NOXIS",
          color = TextSecondary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Action buttons
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Voice Speak Toggle
        IconButton(
          onClick = onToggleAutoSpeak,
          modifier = Modifier
            .size(36.dp)
            .testTag("toggle_voice_button")
        ) {
          Icon(
            imageVector = if (autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
            contentDescription = if (autoSpeak) "Mute Jarvis Voice" else "Unmute Jarvis Voice",
            tint = if (autoSpeak) CyanPrimary else TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Device Hub button
        IconButton(
          onClick = onOpenDeviceHub,
          modifier = Modifier
            .size(36.dp)
            .testTag("device_hub_button")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Device Control Hub",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Routines button
        IconButton(
          onClick = onOpenRoutines,
          modifier = Modifier
            .size(36.dp)
            .testTag("routines_button")
        ) {
          Icon(
            imageVector = Icons.Default.PlayCircleOutline,
            contentDescription = "Automated Routines",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Audit Log button
        IconButton(
          onClick = onOpenAuditLogs,
          modifier = Modifier
            .size(36.dp)
            .testTag("audit_logs_button")
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Audit Logs",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Settings button
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .size(36.dp)
            .testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Bottom Line: Live Telemetry telemetry pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Battery Chip
      TelemetryPill(
        icon = if (deviceStatus.isCharging) Icons.Default.Bolt else null,
        label = "${deviceStatus.batteryPercent}%",
        color = if (deviceStatus.batteryPercent > 20) AccentEmerald else AccentCrimson,
        onClick = onOpenDeviceHub
      )

      // Volume Chip
      TelemetryPill(
        label = "VOL ${deviceStatus.mediaVolumePercent}%",
        color = CyanPrimary,
        onClick = onOpenDeviceHub
      )

      // Torch status
      TelemetryPill(
        icon = if (deviceStatus.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
        label = if (deviceStatus.isTorchOn) "TORCH ON" else "TORCH OFF",
        color = if (deviceStatus.isTorchOn) AccentGold else TextSecondary,
        onClick = onOpenDeviceHub
      )

      // Security Gate Pill
      TelemetryPill(
        icon = Icons.Default.Security,
        label = if (deviceStatus.isSecurityEnforced) "ARMED" else "SAFE",
        color = if (deviceStatus.isSecurityEnforced) AccentCrimson else CyanPrimary,
        onClick = onToggleSecurity
      )
    }
  }
}

@Composable
private fun TelemetryPill(
  label: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
  onClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(SurfaceElevated)
      .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(11.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
    }
    Text(
      text = label,
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}
