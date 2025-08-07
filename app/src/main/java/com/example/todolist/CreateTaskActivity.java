// CreateTaskActivity.java
package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText titleEditText, notesEditText;
    private Button saveButton;
    private ImageButton backButton;
    private RadioGroup priorityRadioGroup;
    private Task taskToEdit;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        // Initialize executor service for database operations
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        titleEditText = findViewById(R.id.task_title);
        notesEditText = findViewById(R.id.task_notes);
        saveButton = findViewById(R.id.save_task_button);
        backButton = findViewById(R.id.btn_back);
        priorityRadioGroup = findViewById(R.id.rg_priority);

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Check if editing an existing task
        taskToEdit = getIntent().getParcelableExtra("task");
        if (taskToEdit != null) {
            populateFieldsForEdit();
            setTitle("Edit Task");
        } else {
            setTitle("Add Task");
            // Set default priority to Medium
            RadioButton mediumPriority = findViewById(R.id.rb_medium);
            mediumPriority.setChecked(true);
        }

        // Setup save button
        saveButton.setOnClickListener(v -> saveTask());
    }

    private void populateFieldsForEdit() {
        titleEditText.setText(taskToEdit.getTitle());
        notesEditText.setText(taskToEdit.getNotes());

        // Set priority radio button based on task priority
        String priority = taskToEdit.getPriority();
        if (priority != null) {
            switch (priority.toLowerCase()) {
                case "low":
                    ((RadioButton) findViewById(R.id.rb_low)).setChecked(true);
                    break;
                case "high":
                    ((RadioButton) findViewById(R.id.rb_high)).setChecked(true);
                    break;
                default:
                    ((RadioButton) findViewById(R.id.rb_medium)).setChecked(true);
                    break;
            }
        }
    }

    private void saveTask() {
        String title = titleEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validation
        if (title.isEmpty()) {
            titleEditText.setError("Task title is required");
            titleEditText.requestFocus();
            return;
        }

        // Get selected priority
        String priority = "Medium"; // default
        int selectedId = priorityRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_low) {
            priority = "Low";
        } else if (selectedId == R.id.rb_high) {
            priority = "High";
        }

        // Save in background thread
        final String finalPriority = priority;
        executorService.execute(() -> {
            try {
                if (taskToEdit == null) {
                    // Create new task
                    String createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    Task newTask = new Task(title, notes, createDate, false);
                    newTask.setPriority(finalPriority);
                    TodoListApplication.database.taskDao().insert(newTask);
                } else {
                    // Update existing task
                    taskToEdit.setTitle(title);
                    taskToEdit.setNotes(notes);
                    taskToEdit.setPriority(finalPriority);
                    TodoListApplication.database.taskDao().update(taskToEdit);
                }

                // Return to main thread to finish activity
                runOnUiThread(() -> {
                    Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}