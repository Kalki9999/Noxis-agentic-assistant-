package com.example.noxis.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.noxis.kernel.SupportedApp
import com.example.ui.theme.AccentCrimson
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun JarvisMainScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val hudState by viewModel.hudState.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val deviceStatus by viewModel.deviceStatus.collectAsState()
  val activeSheet by viewModel.activeSheet.collectAsState()
  val userCallsign by viewModel.userCallsign.collectAsState()
  val autoSpeak by viewModel.autoSpeakEnabled.collectAsState()
  val pendingToolForPin by viewModel.pendingToolForPin.collectAsState()
  val routines by viewModel.routines.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var isListeningAudio by remember { mutableStateOf(false) }

  // Speech Recognizer setup
  val speechRecognizer = remember {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      SpeechRecognizer.createSpeechRecognizer(context)
    } else null
  }

  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  fun startListening() {
    if (speechRecognizer == null) {
      Toast.makeText(context, "Voice recognition service unavailable on device", Toast.LENGTH_SHORT).show()
      return
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PROMPT, "Jarvis is listening...")
    }

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {
        isListeningAudio = true
      }
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rmsdB: Float) {}
      override fun onBufferReceived(buffer: ByteArray?) {}
      override fun onEndOfSpeech() {
        isListeningAudio = false
      }
      override fun onError(error: Int) {
        isListeningAudio = false
      }
      override fun onResults(results: Bundle?) {
        isListeningAudio = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
          val spoken = matches[0]
          viewModel.onUserSpeechInput(spoken)
        }
      }
      override fun onPartialResults(partialResults: Bundle?) {}
      override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    speechRecognizer.startListening(intent)
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      startListening()
    } else {
      Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      speechRecognizer?.destroy()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      HudTopBar(
        deviceStatus = deviceStatus,
        userCallsign = userCallsign,
        autoSpeak = autoSpeak,
        onToggleAutoSpeak = { viewModel.toggleVoiceAutoSpeak() },
        onOpenDeviceHub = { viewModel.openSheet(ActiveSheetDialog.DEVICE_HUB) },
        onOpenRoutines = { viewModel.openSheet(ActiveSheetDialog.ROUTINES) },
        onOpenAuditLogs = { viewModel.openSheet(ActiveSheetDialog.AUDIT_LOGS) },
        onOpenSettings = { viewModel.openSheet(ActiveSheetDialog.SETTINGS) },
        onToggleSecurity = { viewModel.toggleSecurityMode() },
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MidnightBlack)
        .padding(paddingValues)
        .imePadding()
    ) {
      // Arc Reactor Section (compact hero)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(MidnightBlack)
          .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        JarvisArcReactorView(
          hudState = hudState,
          onClick = {
            viewModel.submitCommand("system diagnostics")
          }
        )
      }

      // Conversation Stream
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          ChatMessageCard(
            message = msg,
            onSpeakText = { text ->
              viewModel.voiceManager.speak(text)
            }
          )
        }
      }

      // Quick Actions Pill Row
      QuickActionsRow(
        onActionClick = { cmd ->
          viewModel.submitCommand(cmd)
        }
      )

      // Command Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SurfaceDark)
          .padding(horizontal = 10.dp, vertical = 8.dp)
          .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mic Button
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isListeningAudio) AccentGold.copy(alpha = 0.25f) else SurfaceElevated)
            .border(
              width = 1.dp,
              color = if (isListeningAudio) AccentGold else CyanPrimary.copy(alpha = 0.4f),
              shape = CircleShape
            )
            .clickable {
              val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
              ) == PackageManager.PERMISSION_GRANTED

              if (hasPermission) {
                if (isListeningAudio) {
                  speechRecognizer?.stopListening()
                  isListeningAudio = false
                } else {
                  startListening()
                }
              } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            }
            .testTag("voice_input_mic_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isListeningAudio) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Voice Input",
            tint = if (isListeningAudio) AccentGold else CyanPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Text Field Input
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              text = if (isListeningAudio) "Listening..." else "Command Jarvis (e.g. torch on, volume 80%)...",
              color = TextSecondary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank()) {
                val cmd = inputText
                inputText = ""
                viewModel.submitCommand(cmd)
              }
            }
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = CyanPrimary.copy(alpha = 0.3f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = CyanPrimary
          ),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("command_input_field")
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Halt or Send Button
        if (hudState == JarvisHudState.SPEAKING) {
          IconButton(
            onClick = { viewModel.stopSpeaking() },
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(AccentCrimson)
              .testTag("halt_speech_button")
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Halt Speech", tint = Color.White, modifier = Modifier.size(20.dp))
          }
        } else {
          IconButton(
            onClick = {
              if (inputText.isNotBlank()) {
                val cmd = inputText
                inputText = ""
                viewModel.submitCommand(cmd)
              }
            },
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(CyanPrimary)
              .testTag("transmit_command_button")
          ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Transmit Command", tint = DeepNavy, modifier = Modifier.size(20.dp))
          }
        }
      }
    }

    // Modal Bottom Sheets
    when (activeSheet) {
      ActiveSheetDialog.DEVICE_HUB -> {
        DeviceHubSheet(
          deviceStatus = deviceStatus,
          onToggleTorch = { viewModel.toggleFlashlight() },
          onSetVolume = { vol -> viewModel.setVolume(vol) },
          onMuteVolume = { viewModel.muteVolume() },
          onLaunchApp = { app ->
            viewModel.submitCommand("open ${app.displayName.lowercase()}")
            viewModel.closeSheet()
          },
          onDismiss = { viewModel.closeSheet() }
        )
      }

      ActiveSheetDialog.ROUTINES -> {
        RoutinesSheet(
          routines = routines,
          onExecuteRoutine = { routine -> viewModel.executeRoutine(routine) },
          onCreateRoutine = { name, desc, cmds ->
            viewModel.createRoutine(name, desc, cmds)
          },
          onDeleteRoutine = { routine -> viewModel.deleteRoutine(routine) },
          onDismiss = { viewModel.closeSheet() }
        )
      }

      ActiveSheetDialog.AUDIT_LOGS -> {
        AuditLogSheet(
          logs = auditLogs,
          onClearLogs = { viewModel.clearAuditLogs() },
          onDismiss = { viewModel.closeSheet() }
        )
      }

      ActiveSheetDialog.SETTINGS -> {
        SettingsSheet(
          userCallsign = userCallsign,
          autoSpeak = autoSpeak,
          onUpdateCallsign = { viewModel.updateCallsign(it) },
          onToggleAutoSpeak = { viewModel.toggleVoiceAutoSpeak() },
          onUpdatePin = { viewModel.updatePin(it) },
          onTestVoice = {
            viewModel.voiceManager.speak("All Noxis subsystems operational and responsive, $userCallsign.")
          },
          onDismiss = { viewModel.closeSheet() }
        )
      }

      ActiveSheetDialog.PIN_CONFIRM -> {
        pendingToolForPin?.let { (_, command) ->
          PinConfirmDialog(
            pendingActionDescription = command,
            onConfirmPin = { pin -> viewModel.confirmPinAndExecute(pin) },
            onDismiss = { viewModel.cancelPinConfirm() }
          )
        }
      }

      ActiveSheetDialog.NONE -> {}
    }
  }
}
