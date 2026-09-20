package com.example.noxis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    AuditLogEntity::class,
    RoutineEntity::class,
    UserMemoryEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
  abstract fun auditLogDao(): AuditLogDao
  abstract fun routineDao(): RoutineDao
  abstract fun userMemoryDao(): UserMemoryDao

  companion object {
    @Volatile
    private var INSTANCE: JarvisDatabase? = null

    fun getInstance(context: Context): JarvisDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JarvisDatabase::class.java,
          "jarvis_noxis.db"
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                seedDefaultRoutines(getInstance(context).routineDao())
                seedDefaultMemories(getInstance(context).userMemoryDao())
              }
            }
          })
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private suspend fun seedDefaultRoutines(dao: RoutineDao) {
      if (dao.getRoutineCount() == 0) {
        dao.insertRoutine(
          RoutineEntity(
            name = "Morning Briefing",
            description = "Battery telemetry check, calibrate volume to 70%, and Jarvis morning status briefing",
            iconName = "wb_sunny",
            actionCommands = "battery status\nset volume to 70%\nsystem diagnostics",
            isEnabled = true,
            isDefault = true
          )
        )
        dao.insertRoutine(
          RoutineEntity(
            name = "Focus Protocol",
            description = "Silence media volume, trigger tactical haptic pulse, and engage focus parameters",
            iconName = "do_not_disturb_on",
            actionCommands = "mute volume\nvibrate phone",
            isEnabled = true,
            isDefault = true
          )
        )
        dao.insertRoutine(
          RoutineEntity(
            name = "Power Saver",
            description = "Disengage torch, throttle media volume to 20%, and run battery diagnostics",
            iconName = "battery_saver",
            actionCommands = "turn off flashlight\nset volume to 20%\nbattery status",
            isEnabled = true,
            isDefault = true
          )
        )
        dao.insertRoutine(
          RoutineEntity(
            name = "Night Protocol",
            description = "Ensure flashlight is off, mute audio channels, and log system status",
            iconName = "nightlight",
            actionCommands = "turn off flashlight\nmute volume",
            isEnabled = true,
            isDefault = true
          )
        )
      }
    }

    private suspend fun seedDefaultMemories(dao: UserMemoryDao) {
      dao.setMemory(UserMemoryEntity("user_callsign", "Sir", "identity"))
      dao.setMemory(UserMemoryEntity("ai_protocol", "Jarvis v4.2 // Noxis Phone Core", "system"))
      dao.setMemory(UserMemoryEntity("voice_speed", "1.0", "preferences"))
      dao.setMemory(UserMemoryEntity("auto_speak", "true", "preferences"))
      dao.setMemory(UserMemoryEntity("security_pin", "1234", "security"))
    }
  }
}
