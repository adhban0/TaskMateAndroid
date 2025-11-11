package com.example.taskmate;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;    // <-- added
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
    // private EditText etDue; // removed
    private DatePicker dpDue; // new

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
        // etDue = v.findViewById(R.id.etTaskDue); // removed

        // <-- NEW: find the DatePicker (replace EditText in layout)
        dpDue = v.findViewById(R.id.datePicker);

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_TASK_TITLE);
            String due = args.getString(ARG_TASK_DUE);
            if (title != null) etTitle.setText(title);

            // If a due date was passed, parse and set DatePicker to that date
            if (due != null) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(due, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    // DatePicker month is 0-based, LocalDateTime month is 1-based
                    dpDue.updateDate(ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth());
                } catch (Exception ex) {
                    // ignore parse error and leave DatePicker at default (today)
                }
            }
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

                if (TextUtils.isEmpty(title)) {
                    etTitle.setError("Title required");
                    return;
                }

                // <-- NEW: read date from DatePicker and build LocalDateTime (time set to midnight)
                int day = dpDue.getDayOfMonth();
                int monthZeroBased = dpDue.getMonth(); // 0-based
                int year = dpDue.getYear();
                LocalDateTime due = LocalDateTime.of(year, monthZeroBased + 1, day, 0, 0, 0);

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
