package com.example.noxis.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
  fun getAllLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity)

  @Query("DELETE FROM audit_logs")
  suspend fun clearLogs()
}

@Dao
interface RoutineDao {
  @Query("SELECT * FROM routines ORDER BY id ASC")
  fun getAllRoutines(): Flow<List<RoutineEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoutine(routine: RoutineEntity): Long

  @Update
  suspend fun updateRoutine(routine: RoutineEntity)

  @Delete
  suspend fun deleteRoutine(routine: RoutineEntity)

  @Query("SELECT COUNT(*) FROM routines")
  suspend fun getRoutineCount(): Int
}

@Dao
interface UserMemoryDao {
  @Query("SELECT * FROM user_memories ORDER BY updatedAt DESC")
  fun getAllMemories(): Flow<List<UserMemoryEntity>>

  @Query("SELECT memoryValue FROM user_memories WHERE memoryKey = :key LIMIT 1")
  suspend fun getMemoryValue(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setMemory(memory: UserMemoryEntity)

  @Query("DELETE FROM user_memories WHERE memoryKey = :key")
  suspend fun deleteMemory(key: String)
}
