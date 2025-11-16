package com.example.taskmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    private HomeActivity activity;

    private List<Task> items;
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");;

    public TaskAdapter(List<Task> items, HomeActivity activity) {
        this.items = items;
        this.activity = activity;
    }

    public void setItems(List<Task> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public Task getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public TaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.item_task, parent, false);
        return new TaskVH(v, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {
        Task t = items.get(position);
        holder.bind(t);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class TaskVH extends RecyclerView.ViewHolder {
        TextView title, due;
        CheckBox checkBox;
        private final HomeActivity activity;

        TaskVH(@NonNull View itemView, HomeActivity activity) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTaskTitle);// change ids
            due = itemView.findViewById(R.id.tvTaskDue);
            checkBox = itemView.findViewById(R.id.cbCompleted);
            this.activity = activity;
        }
        void bind(Task t) {
            title.setText(t.getTitle());
            checkBox.setOnCheckedChangeListener(null); // to override later
            checkBox.setChecked(t.isCompleted());

            if (t.getDueDate() != null) {
                due.setText("Due: " + t.getDueDate().format(ISO_FMT));
                due.setVisibility(View.VISIBLE);
            } else {
                due.setVisibility(View.GONE);
            }

            updateBackgroundColor(t);
            itemView.setOnClickListener(v -> activity.onTaskItemClicked(t));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                activity.onTaskItemChecked(t, isChecked);
                updateBackgroundColor(t);
            });
        }
        private void updateBackgroundColor(Task task) {
            int color;
            if (task.isCompleted()) {
                color = ContextCompat.getColor(activity, R.color.white);
            } else {
                if (task.getDueDate() != null) {
                    LocalDate today = LocalDate.now();
                    LocalDate dueDate = task.getDueDate().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, dueDate);

                    if (dueDate.isBefore(today)) {
                        color = ContextCompat.getColor(activity, R.color.task_overdue_dark_red);
                    } else if (daysBetween <= 3) {
                        color = ContextCompat.getColor(activity, R.color.task_due_soon_red);
                    } else {
                        color = ContextCompat.getColor(activity, R.color.task_due_far_green);
                    }
                } else {
                    color = ContextCompat.getColor(activity, R.color.white);
                }
            }
            itemView.setBackgroundColor(color);
        }
    }
}
