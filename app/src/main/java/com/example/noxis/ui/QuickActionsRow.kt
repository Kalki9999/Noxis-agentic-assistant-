package com.example.noxis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary

data class QuickAction(
  val label: String,
  val command: String,
  val icon: ImageVector
)

val defaultQuickActions = listOf(
  QuickAction("Flashlight", "turn on flashlight", Icons.Default.FlashlightOn),
  QuickAction("Battery Diag", "battery status", Icons.Default.BatteryChargingFull),
  QuickAction("Volume 80%", "set volume to 80%", Icons.Default.VolumeUp),
  QuickAction("Mute", "mute volume", Icons.Default.VolumeDown),
  QuickAction("Camera", "open camera", Icons.Default.CameraAlt),
  QuickAction("Morning Brief", "morning routine", Icons.Default.WbSunny),
  QuickAction("Focus Mode", "focus protocol", Icons.Default.Security),
  QuickAction("5m Timer", "set timer for 5 minutes", Icons.Default.Alarm),
  QuickAction("System Scan", "system diagnostics", Icons.Default.QueryStats),
  QuickAction("Clipboard", "read clipboard", Icons.Default.ContentPaste)
)

@Composable
fun QuickActionsRow(
  onActionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  actions: List<QuickAction> = defaultQuickActions
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    actions.forEach { action ->
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(SurfaceElevated)
          .border(0.8.dp, CyanPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
          .clickable { onActionClick(action.command) }
          .testTag("quick_action_${action.label.lowercase().replace(" ", "_")}")
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = action.icon,
          contentDescription = null,
          tint = CyanGlow,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = action.label,
          color = TextPrimary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
