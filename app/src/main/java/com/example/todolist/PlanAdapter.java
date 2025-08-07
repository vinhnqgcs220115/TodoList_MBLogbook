package com.example.todolist;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> planList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Plan plan);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Plan plan);
    }

    public PlanAdapter(List<Plan> planList, OnItemClickListener listener) {
        this.planList = planList;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void updateList(List<Plan> newList) {
        planList.clear();
        planList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public PlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.titleTextView.setText(plan.getTitle());
        holder.dateTextView.setText("Created: " + plan.getCreateDate() + ", Deadline: " + plan.getDeadlineDate());
        holder.goalTextView.setText(plan.getTargetGoal() + " (Progress: " + plan.getProgress() + "%)");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CreatePlanActivity.class);
            intent.putExtra("plan", plan);
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(plan);
                return true;
            }
            return false;
        });

        // Add delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView, goalTextView;
        ImageButton deleteButton; // Add delete button

        public PlanViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.plan_title);
            dateTextView = itemView.findViewById(R.id.plan_date);
            goalTextView = itemView.findViewById(R.id.plan_goal);
            deleteButton = itemView.findViewById(R.id.btn_delete_plan); // Initialize delete button
        }
    }
}