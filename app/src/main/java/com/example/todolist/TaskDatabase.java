package com.example.todolist;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Task.class, Plan.class}, version = 2, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract PlanDao planDao();

    // Migration from version 1 to 2 (adding priority column to tasks table)
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add priority column to tasks table with default value
            database.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT DEFAULT 'Medium'");
        }
    };
}