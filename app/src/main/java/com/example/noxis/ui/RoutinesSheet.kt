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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noxis.data.RoutineEntity
import com.example.ui.theme.AccentCrimson
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesSheet(
  routines: List<RoutineEntity>,
  onExecuteRoutine: (RoutineEntity) -> Unit,
  onCreateRoutine: (name: String, description: String, commands: String) -> Unit,
  onDeleteRoutine: (RoutineEntity) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showCreateDialog by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DeepNavy
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "NOXIS AUTOMATION ROUTINES",
            color = CyanPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "Chained macro sequences executed across phone controls",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
        Row {
          IconButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.testTag("create_routine_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "Create Routine", tint = CyanPrimary)
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_routines_sheet")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (showCreateDialog) {
        CreateRoutineCard(
          onSave = { name, desc, cmds ->
            onCreateRoutine(name, desc, cmds)
            showCreateDialog = false
          },
          onCancel = { showCreateDialog = false }
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(routines) { routine ->
          RoutineItemCard(
            routine = routine,
            onExecute = {
              onExecuteRoutine(routine)
              onDismiss()
            },
            onDelete = { onDeleteRoutine(routine) }
          )
        }
        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun RoutineItemCard(
  routine: RoutineEntity,
  onExecute: () -> Unit,
  onDelete: () -> Unit
) {
  val icon: ImageVector = when (routine.iconName) {
    "wb_sunny" -> Icons.Default.WbSunny
    "do_not_disturb_on" -> Icons.Default.DoNotDisturbOn
    "battery_saver" -> Icons.Default.BatterySaver
    "nightlight" -> Icons.Default.Nightlight
    else -> Icons.Default.SettingsSuggest
  }

  val accentColor = when (routine.iconName) {
    "wb_sunny" -> AccentGold
    "do_not_disturb_on" -> AccentCrimson
    "battery_saver" -> AccentEmerald
    "nightlight" -> CyanPrimary
    else -> CyanPrimary
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(SurfaceElevated)
      .border(0.8.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = routine.name,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = routine.description,
          color = TextSecondary,
          fontSize = 11.sp,
          maxLines = 2
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "STEPS: " + routine.actionCommands.replace("\n", " ➔ "),
          color = accentColor.copy(alpha = 0.8f),
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (!routine.isDefault) {
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
      }
      Button(
        onClick = onExecute,
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
          .testTag("run_routine_${routine.name.lowercase().replace(" ", "_")}")
          .height(34.dp)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "RUN", color = DeepNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun CreateRoutineCard(
  onSave: (name: String, description: String, commands: String) -> Unit,
  onCancel: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var commands by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(SurfaceDark)
      .border(1.dp, CyanPrimary, RoundedCornerShape(8.dp))
      .padding(14.dp)
  ) {
    Text(
      text = "CUSTOM MACRO PIPELINE",
      color = CyanPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Routine Name (e.g. Cinema Mode)") },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanPrimary,
        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("routine_name_input")
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("Description") },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanPrimary,
        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("routine_description_input")
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = commands,
      onValueChange = { commands = it },
      label = { Text("Commands (newline separated: e.g. mute volume, turn on flashlight)") },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanPrimary,
        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      minLines = 2,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("routine_commands_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      Button(
        onClick = onCancel,
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text("Cancel", color = TextSecondary)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          if (name.isNotBlank() && commands.isNotBlank()) {
            onSave(name, description, commands)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.testTag("save_routine_confirm_button")
      ) {
        Text("Save Routine", color = DeepNavy, fontWeight = FontWeight.Bold)
      }
    }
  }
}
