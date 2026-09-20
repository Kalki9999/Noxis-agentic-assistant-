package com.example.noxis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val toolName: String,
  val commandPrompt: String,
  val outputSummary: String,
  val isSuccess: Boolean,
  val durationMs: Long
)

@Entity(tableName = "routines")
data class RoutineEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val description: String,
  val iconName: String,
  val actionCommands: String, // Comma or newline separated commands
  val isEnabled: Boolean = true,
  val isDefault: Boolean = false
)

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
  @PrimaryKey val memoryKey: String,
  val memoryValue: String,
  val category: String = "general",
  val updatedAt: Long = System.currentTimeMillis()
)
