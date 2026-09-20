package com.example.noxis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
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
import com.example.ui.theme.BorderGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageCard(
  message: ChatMessage,
  onSpeakText: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val timeStr = timeFormat.format(Date(message.timestamp))

  when (message.sender) {
    MessageSender.USER -> {
      Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 4.dp, bottomEnd = 12.dp))
            .background(SurfaceElevated)
            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 4.dp, bottomEnd = 12.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "COMMAND INVOCATION",
              color = CyanPrimary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = timeStr,
              color = TextSecondary,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = message.text,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    MessageSender.JARVIS -> {
      Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(DeepNavy.copy(alpha = 0.9f))
            .border(1.2.dp, BorderGlow, RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(12.dp)
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "JARVIS // ASSISTANT",
                color = CyanPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = timeStr,
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(
                onClick = { onSpeakText(message.text) },
                modifier = Modifier.size(22.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = "Speak Response",
                  tint = CyanGlow,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Body Text
          Text(
            text = message.text,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 19.sp
          )

          // Embedded Tool Result Card
          message.toolResult?.let { toolRes ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceDark)
                .border(
                  width = 0.8.dp,
                  color = if (toolRes.isSuccess) AccentEmerald.copy(alpha = 0.4f) else AccentCrimson.copy(alpha = 0.4f),
                  shape = RoundedCornerShape(6.dp)
                )
                .padding(8.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = if (toolRes.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                      contentDescription = null,
                      tint = if (toolRes.isSuccess) AccentEmerald else AccentCrimson,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "NOXIS TOOL: ${toolRes.toolName.uppercase()}",
                      color = if (toolRes.isSuccess) AccentEmerald else AccentCrimson,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Speed,
                      contentDescription = null,
                      tint = TextSecondary,
                      modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                      text = "${toolRes.latencyMs}ms",
                      color = TextSecondary,
                      fontSize = 9.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = toolRes.outputMessage,
                  color = TextPrimary.copy(alpha = 0.9f),
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    MessageSender.NOXIS_KERNEL -> {
      Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
            .border(0.7.dp, CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "KERNEL PIPELINE STEP",
              color = CyanPrimary.copy(alpha = 0.7f),
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = timeStr,
              color = TextSecondary,
              fontSize = 8.sp,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = message.text,
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
