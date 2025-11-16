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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddEditTaskDialogFragment extends DialogFragment {
    private static final String ARG_TASK_ID = "arg_task_id";
    private static final String ARG_TASK_TITLE = "arg_task_title";
    private static final String ARG_TASK_DUE = "arg_task_due";
    //h

    private EditText etTitle;
    private SwitchCompat switchDueDate;
    private DatePicker dpDue;

    //creates dialog fragments
    public static AddEditTaskDialogFragment newInstance(@Nullable Task task) {
        AddEditTaskDialogFragment f = new AddEditTaskDialogFragment();
        Bundle b = new Bundle();//pass data between android components like intent
        if (task != null) {
            b.putInt(ARG_TASK_ID, task.getId());
            b.putString(ARG_TASK_TITLE, task.getTitle());
            b.putString(ARG_TASK_DUE, task.getDueDate() == null ? null : task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        f.setArguments(b);
        return f;
    }


// called when dialog is created in previous method
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View v = li.inflate(R.layout.dialog_add_task, null);
        etTitle = v.findViewById(R.id.etTaskTitle);
        switchDueDate = v.findViewById(R.id.switchDueDate);
        dpDue = v.findViewById(R.id.datePicker);
        long now = System.currentTimeMillis();
        dpDue.setMinDate(now);

        Bundle args = getArguments();
        if (args != null) {//null when task is created
            String title = args.getString(ARG_TASK_TITLE);
            String due = args.getString(ARG_TASK_DUE);
            if (title != null) etTitle.setText(title);
            // if due is null, dp will stay invisible and switch will stay unchecked (their defaults)
            if (due != null) {
                switchDueDate.setChecked(true);
                dpDue.setVisibility(View.VISIBLE);
                try {
                    LocalDateTime ldt = LocalDateTime.parse(due, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    dpDue.updateDate(ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth());
                } catch (Exception ex) {
                }
            }
        }
        switchDueDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dpDue.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        // builder of the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(v)
                .setTitle(args != null && args.containsKey(ARG_TASK_ID) ? "Edit task" : "Add task")
                .setNegativeButton("Cancel", (d, i) -> dismiss())// i means which button
                .setPositiveButton("Save", null); // override later to prevent auto-dismiss

        AlertDialog dialog = builder.create(); // build the dialog and store it in this alertdialog object
        dialog.setOnShowListener(d -> { // onshowlistener executes code when dialog is shown
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setOnClickListener(view -> {
                String title = etTitle.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    etTitle.setError("Title required");
                    return;
                }

                LocalDateTime due = null;
                if (switchDueDate.isChecked()) {
                    int day = dpDue.getDayOfMonth();
                    int monthZeroBased = dpDue.getMonth();
                    int year = dpDue.getYear();
                    due = LocalDateTime.of(year, monthZeroBased + 1, day, 0, 0, 0);
                }
                int id = args != null && args.containsKey(ARG_TASK_ID) ? args.getInt(ARG_TASK_ID) : 0;
                // username will be set by MainActivity when saving
                Task t = new Task(id, title, due, false, "");
                HomeActivity activity = (HomeActivity) getActivity();// get current activity not a new class
                if (activity != null) {
                    activity.saveTask(t);
                }
                dismiss(); //close the fragment
            });
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // to establish a connection with the main activity
    }
}
