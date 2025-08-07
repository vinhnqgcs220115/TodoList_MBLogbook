package com.example.todolist;

import android.app.Application;
import androidx.room.Room;

public class TodoListApplication extends Application {
    public static TaskDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this,
                        TaskDatabase.class, "task_database")
                .addMigrations(TaskDatabase.MIGRATION_1_2)
                .build();
    }
}