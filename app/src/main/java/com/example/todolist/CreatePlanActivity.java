package com.example.todolist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreatePlanActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, targetGoalEditText;
    private Button saveButton, deadlineDateButton;
    private ImageButton backButton;
    private Spinner orientationSpinner;
    private SeekBar progressSeekBar;
    private TextView progressTextView;
    private Plan planToEdit;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        // Initialize executor service for database operations
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        titleEditText = findViewById(R.id.plan_title);
        descriptionEditText = findViewById(R.id.plan_description);
        targetGoalEditText = findViewById(R.id.plan_target_goal);
        deadlineDateButton = findViewById(R.id.plan_deadline_date);
        orientationSpinner = findViewById(R.id.plan_orientation);
        saveButton = findViewById(R.id.save_plan_button);
        backButton = findViewById(R.id.btn_back);
        progressSeekBar = findViewById(R.id.plan_progress);
        progressTextView = findViewById(R.id.progress_text);

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Setup spinner with orientation options
        setupOrientationSpinner();

        // Setup date picker
        setupDatePicker();

        // Check if editing an existing plan
        planToEdit = getIntent().getParcelableExtra("plan");
        if (planToEdit != null) {
            populateFieldsForEdit();
            setTitle("Edit Plan");
        } else {
            setTitle("Add Plan");
            progressSeekBar.setProgress(0);
            updateProgressText(0);
        }

        // Setup progress seekbar
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateProgressText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup save button
        saveButton.setOnClickListener(v -> savePlan());
    }

    private void setupOrientationSpinner() {
        String[] orientations = {
                "Personal Development",
                "Career & Business",
                "Health & Fitness",
                "Education & Learning",
                "Financial Goals",
                "Relationships",
                "Travel & Adventure",
                "Creative Projects",
                "Home & Lifestyle",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, orientations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orientationSpinner.setAdapter(adapter);
    }

    private void setupDatePicker() {
        deadlineDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // If editing and has existing date, parse it
            if (planToEdit != null && planToEdit.getDeadlineDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date existingDate = sdf.parse(planToEdit.getDeadlineDate());
                    calendar.setTime(existingDate);
                } catch (ParseException e) {
                    // Use current date if parsing fails
                }
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        deadlineDateButton.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void populateFieldsForEdit() {
        titleEditText.setText(planToEdit.getTitle());
        descriptionEditText.setText(planToEdit.getDescription());
        targetGoalEditText.setText(planToEdit.getTargetGoal());
        deadlineDateButton.setText(planToEdit.getDeadlineDate());

        // Set spinner selection
        String orientation = planToEdit.getOrientation();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) orientationSpinner.getAdapter();
        int position = adapter.getPosition(orientation);
        if (position >= 0) {
            orientationSpinner.setSelection(position);
        }

        progressSeekBar.setProgress(planToEdit.getProgress());
        updateProgressText(planToEdit.getProgress());
    }

    private void savePlan() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String targetGoal = targetGoalEditText.getText().toString().trim();
        String deadlineDate = deadlineDateButton.getText().toString().trim();
        String orientation = orientationSpinner.getSelectedItem().toString();
        int progress = progressSeekBar.getProgress();

        // Validation
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return;
        }

        if (targetGoal.isEmpty()) {
            targetGoalEditText.setError("Target goal is required");
            targetGoalEditText.requestFocus();
            return;
        }

        if (deadlineDate.equals("Select Deadline Date") || !isValidDate(deadlineDate)) {
            Toast.makeText(this, "Please select a valid future deadline date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save in background thread
        executorService.execute(() -> {
            try {
                if (planToEdit == null) {
                    // Create new plan
                    String createDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    Plan newPlan = new Plan(title, description, targetGoal, createDate, deadlineDate, orientation);
                    newPlan.setProgress(progress);
                    TodoListApplication.database.planDao().insert(newPlan);
                } else {
                    // Update existing plan
                    planToEdit.setTitle(title);
                    planToEdit.setDescription(description);
                    planToEdit.setTargetGoal(targetGoal);
                    planToEdit.setDeadlineDate(deadlineDate);
                    planToEdit.setOrientation(orientation);
                    planToEdit.setProgress(progress);
                    TodoListApplication.database.planDao().update(planToEdit);
                }

                // Return to main thread to finish activity
                runOnUiThread(() -> {
                    Toast.makeText(this, "Plan saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equals("Select Deadline Date")) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date deadline = sdf.parse(dateStr);
            Date today = new Date();
            return deadline.after(today);
        } catch (ParseException e) {
            return false;
        }
    }

    private void updateProgressText(int progress) {
        progressTextView.setText(progress + "%");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}