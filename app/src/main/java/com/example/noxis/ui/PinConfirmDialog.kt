package com.example.noxis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCrimson
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinConfirmDialog(
  pendingActionDescription: String,
  onConfirmPin: (String) -> Boolean,
  onDismiss: () -> Unit
) {
  var pin by remember { mutableStateOf("") }
  var hasError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          val success = onConfirmPin(pin)
          if (!success) {
            hasError = true
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.testTag("pin_confirm_submit_button")
      ) {
        Text("Authorize", color = DeepNavy, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    },
    dismissButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.testTag("pin_confirm_cancel_button")
      ) {
        Text("Abort", color = TextSecondary, fontFamily = FontFamily.Monospace)
      }
    },
    containerColor = DeepNavy,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Security, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "SECURITY CLEARANCE REQUIRED",
          color = AccentGold,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
      }
    },
    text = {
      Column {
        Text(
          text = "The requested phone control operation requires security clearance:",
          color = TextPrimary,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceDark)
            .padding(8.dp)
        ) {
          Text(
            text = pendingActionDescription,
            color = CyanPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = pin,
          onValueChange = {
            if (it.length <= 6) {
              pin = it
              hasError = false
            }
          },
          label = { Text("Enter Security PIN (Default: 1234)") },
          visualTransformation = PasswordVisualTransformation(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          isError = hasError,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("security_pin_dialog_input")
        )
        if (hasError) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Authorization failed. Incorrect PIN.",
            color = AccentCrimson,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  )
}
