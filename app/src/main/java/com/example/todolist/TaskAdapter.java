package com.example.todolist;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onCheckboxClick(Task task, boolean isChecked);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void updateList(List<Task> newList) {
        taskList.clear();
        taskList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.dateTextView.setText(task.getCreateDate());
        holder.checkBox.setChecked(task.isCompleted());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CreateTaskActivity.class);
            intent.putExtra("task", task);
            v.getContext().startActivity(intent);
        });

        holder.checkBox.setOnClickListener(v -> listener.onCheckboxClick(task, holder.checkBox.isChecked()));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(task);
                return true;
            }
            return false;
        });

        // Add delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView;
        CheckBox checkBox;
        ImageButton deleteButton; // Add delete button

        public TaskViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.task_title);
            dateTextView = itemView.findViewById(R.id.task_date);
            checkBox = itemView.findViewById(R.id.task_checkbox);
            deleteButton = itemView.findViewById(R.id.btn_delete_task); // Initialize delete button
        }
    }
}