package com.a3.yearlyprogess.widgets.manager.eventManager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Converters
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event

@Database(entities = [Event::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
  abstract fun eventDao(): EventDao

  companion object {
    @Volatile private var INSTANCE: EventDatabase? = null

    fun getDatabase(context: Context): EventDatabase {
      val tempInstance = INSTANCE
      if (tempInstance != null) {
        return tempInstance
      }
      synchronized(this) {
        val instance =
            Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database",
                )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration(true)
                .build()
        INSTANCE = instance
        return instance
      }
    }

    private val MIGRATION_1_2 =
        object : Migration(1, 2) {
          override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE event_table ADD COLUMN repeatEventDays TEXT NOT NULL DEFAULT ''",
            )
          }
        }

    private val MIGRATION_2_3 =
        object : Migration(2, 3) {
          override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE event_table ADD COLUMN hasWeekDays INTEGER NOT NULL DEFAULT 0",
            )
          }
        }

    private val MIGRATION_3_4 =
        object : Migration(3, 4) {
          override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE event_table ADD COLUMN backgroundImageUri TEXT DEFAULT NULL",
            )
          }
        }
  }
}
