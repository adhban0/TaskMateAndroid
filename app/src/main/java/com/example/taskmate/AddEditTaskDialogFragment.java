package com.example.taskmate;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddEditTaskDialogFragment extends DialogFragment {

    public interface OnSaveListener {
        void onSave(Task task); // task.id == 0 for new
    }

    private static final String ARG_TASK_ID = "arg_task_id";
    private static final String ARG_TASK_TITLE = "arg_task_title";
    private static final String ARG_TASK_DUE = "arg_task_due"; // ISO string or null
    private OnSaveListener listener;

    private EditText etTitle;
    private EditText etDue;

    public static AddEditTaskDialogFragment newInstance(@Nullable Task task) {
        AddEditTaskDialogFragment f = new AddEditTaskDialogFragment();
        Bundle b = new Bundle();
        if (task != null) {
            b.putInt(ARG_TASK_ID, task.getId());
            b.putString(ARG_TASK_TITLE, task.getTitle());
            b.putString(ARG_TASK_DUE, task.getDueDate() == null ? null : task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        f.setArguments(b);
        return f;
    }

    public void setOnSaveListener(OnSaveListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View v = li.inflate(R.layout.dialog_add_task, null);
        etTitle = v.findViewById(R.id.etTaskTitle);
        etDue = v.findViewById(R.id.etTaskDue);

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_TASK_TITLE);
            String due = args.getString(ARG_TASK_DUE);
            if (title != null) etTitle.setText(title);
            if (due != null) etDue.setText(due);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(v)
                .setTitle(args != null && args.containsKey(ARG_TASK_ID) ? "Edit task" : "Add task")
                .setNegativeButton("Cancel", (d, i) -> dismiss())
                .setPositiveButton("Save", null); // override later to prevent auto-dismiss

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button bOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            bOk.setOnClickListener(view -> {
                String title = etTitle.getText().toString().trim();
                String dueText = etDue.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    etTitle.setError("Title required");
                    return;
                }
                LocalDateTime due = null;
                if (!TextUtils.isEmpty(dueText)) {
                    try {
                        due = LocalDateTime.parse(dueText, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (Exception ex) {
                        etDue.setError("Use ISO format: yyyy-MM-ddTHH:mm:ss");
                        return;
                    }
                }
                int id = args != null && args.containsKey(ARG_TASK_ID) ? args.getInt(ARG_TASK_ID) : 0;
                // username will be set by MainActivity when saving
                Task t = new Task(id, title, due, false, "");
                if (listener != null) {
                    listener.onSave(t);
                }
                dismiss();
            });
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // listener set by activity explicitly
    }
}
