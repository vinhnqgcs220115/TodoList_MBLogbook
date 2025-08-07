package com.example.todolist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskFragment extends Fragment {

    private static final String TAG = "TaskFragment";
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        taskRecyclerView = view.findViewById(R.id.task_recycler_view);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == taskAdapter.getItemCount() - 1) {
                    loadMoreTasks();
                }
            }
        });
        setupTaskObserver();
        loadTasks();

        view.findViewById(R.id.add_task_button).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreateTaskActivity.class));
        });

        // Auto-delete tasks older than 7 days on a background thread
        autoDeleteOldTasks();

        return view;
    }

    private void setupTaskObserver() {
        TodoListApplication.database.taskDao().getAllTasks().observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                if (taskAdapter == null) {
                    taskAdapter = new TaskAdapter(tasks.subList(0, Math.min(PAGE_SIZE, tasks.size())), new TaskAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Task task) {
                            Intent intent = new Intent(getContext(), CreateTaskActivity.class);
                            intent.putExtra("task", task);
                            startActivity(intent);
                        }

                        @Override
                        public void onCheckboxClick(Task task, boolean isChecked) {
                            task.setCompleted(isChecked);
                            TodoListApplication.database.taskDao().update(task);
                            Log.d(TAG, "Task " + task.getTitle() + " updated to completed: " + isChecked);
                        }
                    });
                    taskAdapter.setOnItemLongClickListener(task -> showDeleteConfirmation(task));
                    taskRecyclerView.setAdapter(taskAdapter);
                } else {
                    updatePage(tasks);
                }
            }
        });
    }

    private void loadTasks() {
        // Initial load handled by observer
    }

    private void loadMoreTasks() {
        currentPage++;
        List<Task> allTasks = TodoListApplication.database.taskDao().getAllTasks().getValue();
        if (allTasks != null) {
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, allTasks.size());
            updatePage(allTasks.subList(0, end));
        }
    }

    private void updatePage(List<Task> tasks) {
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, tasks.size());
        taskAdapter.updateList(tasks.subList(start, end));
    }

    private void autoDeleteOldTasks() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -7);
                String deleteDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                TodoListApplication.database.taskDao().deleteOldTasks(deleteDate);
                return null;
            }
        }.execute();
    }

    private void showDeleteConfirmation(Task task) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete '" + task.getTitle() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TodoListApplication.database.taskDao().delete(task);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}