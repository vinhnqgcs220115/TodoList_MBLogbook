package com.example.todolist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class PlanFragment extends Fragment {

    private RecyclerView planRecyclerView;
    private PlanAdapter planAdapter;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        planRecyclerView = view.findViewById(R.id.plan_recycler_view);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        planRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == planAdapter.getItemCount() - 1) {
                    loadMorePlans();
                }
            }
        });
        setupPlanObserver();
        loadPlans();

        view.findViewById(R.id.add_plan_button).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreatePlanActivity.class));
        });

        // Auto-delete plans 30 days after deadline on a background thread
        autoDeleteOldPlans();

        return view;
    }

    private void setupPlanObserver() {
        TodoListApplication.database.planDao().getAllPlans().observe(getViewLifecycleOwner(), new Observer<List<Plan>>() {
            @Override
            public void onChanged(List<Plan> plans) {
                if (planAdapter == null) {
                    planAdapter = new PlanAdapter(plans.subList(0, Math.min(PAGE_SIZE, plans.size())), new PlanAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(Plan plan) {
                            Intent intent = new Intent(getContext(), CreatePlanActivity.class);
                            intent.putExtra("plan", plan);
                            startActivity(intent);
                        }
                    });
                    planAdapter.setOnItemLongClickListener(plan -> showDeleteConfirmation(plan));
                    planRecyclerView.setAdapter(planAdapter);
                } else {
                    updatePage(plans);
                }
            }
        });
    }

    private void loadPlans() {
        // Initial load handled by observer
    }

    private void loadMorePlans() {
        currentPage++;
        List<Plan> allPlans = TodoListApplication.database.planDao().getAllPlans().getValue();
        if (allPlans != null) {
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, allPlans.size());
            updatePage(allPlans.subList(0, end));
        }
    }

    private void updatePage(List<Plan> plans) {
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, plans.size());
        planAdapter.updateList(plans.subList(start, end));
    }

    private void autoDeleteOldPlans() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -30);
                String deleteDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                TodoListApplication.database.planDao().deleteOldPlans(deleteDate);
                return null;
            }
        }.execute();
    }

    private void showDeleteConfirmation(Plan plan) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete '" + plan.getTitle() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TodoListApplication.database.planDao().delete(plan);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}