package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(int taskId);

    @Query("DELETE FROM tasks")
    void deleteAllTasks();

    @Query("DELETE FROM tasks WHERE createDate < :date")
    void deleteOldTasks(String date);
}