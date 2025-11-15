package com.example.taskmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {

    public interface OnItemInteraction {
        void onItemClicked(Task task);
        void onItemChecked(Task task, boolean checked);
        void onItemLongPressed(int position);
    }

    private List<Task> items;
    private OnItemInteraction listener;
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");;

    public TaskAdapter(List<Task> items, OnItemInteraction listener) {
        this.items = items;
        this.listener = listener;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);// add context as a variable and pass it to the constructor
        return new TaskVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskVH holder, int position) {
        Task t = items.get(position);
        holder.title.setText(t.getTitle());
        if (t.getDueDate() != null) {
            holder.due.setText("Due: " + t.getDueDate().format(ISO_FMT));
            holder.due.setVisibility(View.VISIBLE);
        } else {
            holder.due.setVisibility(View.GONE);
        }
        holder.checkBox.setOnCheckedChangeListener(null); // avoid updating data of previous recycled items (detach the listener)
        holder.checkBox.setChecked(t.isCompleted());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(t);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongPressed(holder.getAdapterPosition());
            return true;
        });
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onItemChecked(t, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class TaskVH extends RecyclerView.ViewHolder {
        TextView title, due;
        CheckBox checkBox;

        TaskVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTaskTitle);// change ids
            due = itemView.findViewById(R.id.tvTaskDue);
            checkBox = itemView.findViewById(R.id.cbCompleted);
        }
    }
}
