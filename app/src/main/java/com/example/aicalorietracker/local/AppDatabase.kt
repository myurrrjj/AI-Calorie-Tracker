
package com.example.aicalorietracker.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MealLog::class,SavedMeal::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun savedMealDao(): SavedMealDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `savedMeals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userRequest` TEXT NOT NULL, `aiResponse` TEXT NOT NULL, `imagePath` TEXT, `frequency` INTEGER NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fat` INTEGER NOT NULL, `fiber` INTEGER NOT NULL, `sugar` INTEGER NOT NULL, `vitaminA` REAL NOT NULL, `vitaminC` REAL NOT NULL, `vitaminD` REAL NOT NULL, `iron` REAL NOT NULL, `calcium` REAL NOT NULL, `sodium` REAL NOT NULL, `potassium` REAL NOT NULL)"
                )
            }
        }

        private val MIGRATION3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meal_logs ADD COLUMN quantity REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE savedMeals ADD COLUMN quantity REAL NOT NULL DEFAULT 1.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calorie_tracker_database"
                )
                    .addMigrations(MIGRATION2_3, MIGRATION3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}