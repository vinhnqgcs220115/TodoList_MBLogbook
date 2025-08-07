package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface PlanDao {
    @Insert
    void insert(Plan plan);

    @Update
    void update(Plan plan);

    @Delete
    void delete(Plan plan);

    @Query("SELECT * FROM plans ORDER BY id DESC")
    LiveData<List<Plan>> getAllPlans();

    @Query("DELETE FROM plans WHERE id = :planId")
    void deletePlan(int planId);

    @Query("DELETE FROM plans")
    void deleteAllPlans();

    @Query("DELETE FROM plans WHERE deadlineDate < :date")
    void deleteOldPlans(String date);
}