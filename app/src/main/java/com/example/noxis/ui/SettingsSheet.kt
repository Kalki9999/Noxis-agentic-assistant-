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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
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
fun SettingsSheet(
  userCallsign: String,
  autoSpeak: Boolean,
  onUpdateCallsign: (String) -> Unit,
  onToggleAutoSpeak: () -> Unit,
  onUpdatePin: (String) -> Unit,
  onTestVoice: () -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var callsignInput by remember { mutableStateOf(userCallsign) }
  var pinInput by remember { mutableStateOf("") }
  var pinSavedNotice by remember { mutableStateOf(false) }

  val isGeminiConfigured = try {
    BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
  } catch (_: Exception) {
    false
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "JARVIS CONFIGURATION",
            color = CyanPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "Autonomous persona & Noxis kernel parameters",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_settings_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // User Callsign Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceElevated)
          .border(0.8.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Person, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "User Callsign / Title",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Sir", "Commander", "Captain", "Doctor").forEach { title ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (callsignInput == title) CyanPrimary else SurfaceDark)
                .clickable {
                  callsignInput = title
                  onUpdateCallsign(title)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = title,
                color = if (callsignInput == title) DeepNavy else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = callsignInput,
            onValueChange = { callsignInput = it },
            label = { Text("Custom Title") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanPrimary,
              unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("callsign_text_field")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = { onUpdateCallsign(callsignInput) },
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.testTag("save_callsign_button")
          ) {
            Text("Save", color = DeepNavy, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Voice Synthesizer Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceElevated)
          .border(0.8.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Speech Synthesis (TTS)",
              color = TextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Switch(
            checked = autoSpeak,
            onCheckedChange = { onToggleAutoSpeak() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = CyanPrimary,
              checkedTrackColor = DeepNavy,
              uncheckedThumbColor = TextSecondary,
              uncheckedTrackColor = SurfaceDark
            ),
            modifier = Modifier.testTag("settings_auto_speak_switch")
          )
        }

        Text(
          text = if (autoSpeak) "Jarvis will speak responses aloud automatically." else "Responses will remain silent/text-only.",
          color = TextSecondary,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onTestVoice,
          colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier.testTag("test_voice_button")
        ) {
          Text("Test Jarvis Voice Output", color = CyanPrimary, fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Security PIN Management Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceElevated)
          .border(0.8.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Noxis Security Gate PIN",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Default PIN is 1234. Used to verify elevated commands.",
          color = TextSecondary,
          fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = pinInput,
            onValueChange = { if (it.length <= 6) pinInput = it },
            label = { Text("New 4-6 Digit PIN") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentGold,
              unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("pin_code_input")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (pinInput.length >= 4) {
                onUpdatePin(pinInput)
                pinSavedNotice = true
                pinInput = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.testTag("save_pin_button")
          ) {
            Text("Update", color = DeepNavy, fontWeight = FontWeight.Bold)
          }
        }
        if (pinSavedNotice) {
          Spacer(modifier = Modifier.height(4.dp))
          Text("Security PIN successfully updated.", color = AccentEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Intelligence Core Status Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceDark)
          .border(0.8.dp, if (isGeminiConfigured) AccentEmerald.copy(alpha = 0.3f) else AccentGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Info, contentDescription = null, tint = if (isGeminiConfigured) AccentEmerald else AccentGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "AI Core Engine Status",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (isGeminiConfigured) {
            "Active: Gemini 3.5 Flash online via AI Studio Secrets. Autonomous fallback engine primed."
          } else {
            "Autonomous Mode: Local high-speed intent matching active. To enable cloud Gemini 3.5 Flash, supply GEMINI_API_KEY in Secrets panel."
          },
          color = TextSecondary,
          fontSize = 11.sp
        )
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
