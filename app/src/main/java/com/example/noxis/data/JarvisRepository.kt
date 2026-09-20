package com.example.noxis.data

import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val database: JarvisDatabase) {

  val auditLogs: Flow<List<AuditLogEntity>> = database.auditLogDao().getAllLogs()
  val routines: Flow<List<RoutineEntity>> = database.routineDao().getAllRoutines()
  val memories: Flow<List<UserMemoryEntity>> = database.userMemoryDao().getAllMemories()

  suspend fun insertAuditLog(log: AuditLogEntity) {
    database.auditLogDao().insertLog(log)
  }

  suspend fun clearAuditLogs() {
    database.auditLogDao().clearLogs()
  }

  suspend fun saveRoutine(routine: RoutineEntity): Long {
    return database.routineDao().insertRoutine(routine)
  }

  suspend fun updateRoutine(routine: RoutineEntity) {
    database.routineDao().updateRoutine(routine)
  }

  suspend fun deleteRoutine(routine: RoutineEntity) {
    database.routineDao().deleteRoutine(routine)
  }

  suspend fun getMemory(key: String): String? {
    return database.userMemoryDao().getMemoryValue(key)
  }

  suspend fun saveMemory(key: String, value: String, category: String = "general") {
    database.userMemoryDao().setMemory(
      UserMemoryEntity(
        memoryKey = key,
        memoryValue = value,
        category = category,
        updatedAt = System.currentTimeMillis()
      )
    )
  }
}
