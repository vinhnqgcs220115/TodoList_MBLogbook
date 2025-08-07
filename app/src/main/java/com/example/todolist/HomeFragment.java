package com.example.todolist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView planRecyclerView, taskRecyclerView;
    private PlanAdapter planAdapter;
    private TaskAdapter taskAdapter;
    private TextView quoteTextView;
    private ImageButton btnAddTask, btnAddPlan;
    private TextView tvViewAllTasks, tvViewAllPlans;
    private ExecutorService executorService;

    private static final List<String> QUOTES = Arrays.asList(
            "The way to get started is to quit talking and begin doing.",
            "Stay focused and keep moving forward",
            "Every day is a new opportunity",
            "Success is the sum of small efforts",
            "Don't watch the clock; do what it does. Keep going.",
            "The future depends on what you do today.",
            "Your limitation—it's only your imagination.",
            "Push yourself, because no one else is going to do it for you."
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize executor service
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        initializeViews(view);

        // Set random quote
        setRandomQuote();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Setup observers
        setupPlanObserver();
        setupTaskObserver();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        quoteTextView = view.findViewById(R.id.quote_text);
        planRecyclerView = view.findViewById(R.id.plan_recycler_view);
        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        btnAddTask = view.findViewById(R.id.btn_add_task);
        btnAddPlan = view.findViewById(R.id.btn_add_plan);
        tvViewAllTasks = view.findViewById(R.id.tv_view_all_tasks);
        tvViewAllPlans = view.findViewById(R.id.tv_view_all_plans);
    }

    private void setupRecyclerViews() {
        planRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupClickListeners() {
        // Add Task button click listener
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateTaskActivity.class);
            startActivity(intent);
        });

        // Add Plan button click listener
        btnAddPlan.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreatePlanActivity.class);
            startActivity(intent);
        });

        // View All Tasks click listener
        tvViewAllTasks.setOnClickListener(v -> {
            // Navigate to TaskFragment
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                // Switch to Task tab
                ((MainActivity) getActivity()).findViewById(R.id.bottom_navigation)
                        .findViewById(R.id.nav_task).performClick();
            }
        });

        // View All Plans click listener
        tvViewAllPlans.setOnClickListener(v -> {
            // Navigate to PlanFragment
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                // Switch to Plan tab
                ((MainActivity) getActivity()).findViewById(R.id.bottom_navigation)
                        .findViewById(R.id.nav_plan).performClick();
            }
        });
    }

    private void setRandomQuote() {
        Random random = new Random();
        String selectedQuote = QUOTES.get(random.nextInt(QUOTES.size()));
        quoteTextView.setText(selectedQuote);
    }

    private void setupPlanObserver() {
        TodoListApplication.database.planDao().getAllPlans().observe(getViewLifecycleOwner(), new Observer<List<Plan>>() {
            @Override
            public void onChanged(List<Plan> plans) {
                if (planAdapter == null) {
                    planAdapter = new PlanAdapter(plans, new PlanAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Plan plan) {
                            Intent intent = new Intent(getContext(), CreatePlanActivity.class);
                            intent.putExtra("plan", plan);
                            startActivity(intent);
                        }
                    });

                    // Set long click listener for delete functionality
                    planAdapter.setOnItemLongClickListener(plan -> showDeleteConfirmation(plan));
                    planRecyclerView.setAdapter(planAdapter);
                } else {
                    planAdapter.updateList(plans);
                }
            }
        });
    }

    private void setupTaskObserver() {
        TodoListApplication.database.taskDao().getAllTasks().observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                if (taskAdapter == null) {
                    taskAdapter = new TaskAdapter(tasks, new TaskAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Task task) {
                            Intent intent = new Intent(getContext(), CreateTaskActivity.class);
                            intent.putExtra("task", task);
                            startActivity(intent);
                        }

                        @Override
                        public void onCheckboxClick(Task task, boolean isChecked) {
                            task.setCompleted(isChecked);
                            // Update in background thread
                            executorService.execute(() -> {
                                TodoListApplication.database.taskDao().update(task);
                            });
                        }
                    });

                    // Set long click listener for delete functionality
                    taskAdapter.setOnItemLongClickListener(task -> showDeleteConfirmation(task));
                    taskRecyclerView.setAdapter(taskAdapter);
                } else {
                    taskAdapter.updateList(tasks);
                }
            }
        });
    }

    private void showDeleteConfirmation(Object item) {
        String title = (item instanceof Task) ? ((Task) item).getTitle() : ((Plan) item).getTitle();
        String type = (item instanceof Task) ? "Task" : "Plan";

        new AlertDialog.Builder(getContext())
                .setTitle("Delete " + type)
                .setMessage("Are you sure you want to delete '" + title + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete in background thread
                    executorService.execute(() -> {
                        if (item instanceof Task) {
                            TodoListApplication.database.taskDao().delete((Task) item);
                        } else if (item instanceof Plan) {
                            TodoListApplication.database.planDao().delete((Plan) item);
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}