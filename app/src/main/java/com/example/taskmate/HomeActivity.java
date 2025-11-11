package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DBHelper db;
    private RecyclerView rv;
    private TaskAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    private boolean showCompleted = false;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = new DBHelper(this);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(tasks, new TaskAdapter.OnItemInteraction() {
            @Override
            public void onItemClicked(Task task) {
                openEditDialog(task);
            }

            @Override
            public void onItemChecked(Task task, boolean checked) {
                db.updateTaskCompletion(task.getId(), checked);
                // update local list and refresh
                loadTasks();
            }

            @Override
            public void onItemLongPressed(int position) {
                // not using selection now; placeholder if you want multi-select
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> openAddDialog());

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            Task recentlyDeletedTask = null;
            int recentlyDeletedPosition = -1;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                recentlyDeletedTask = tasks.get(pos);
                recentlyDeletedPosition = pos;

                db.deleteTask(recentlyDeletedTask.getId());
                loadTasks();

                Snackbar.make(findViewById(R.id.rootCoordinator), "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            // re-insert task (simple approach)
                            Task t = recentlyDeletedTask;
                            db.insertTask(t.getTitle(),  t.getDueDate(), t.isCompleted(), username);
                            loadTasks();
                        }).show();
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(rv);

        loadTasks();
    }

    private void openAddDialog() {
        AddEditTaskDialogFragment dlg = AddEditTaskDialogFragment.newInstance(null);
        dlg.setOnSaveListener(task -> {
            db.insertTask(task.getTitle(),  task.getDueDate(), false, username);
            loadTasks();
        });
        dlg.show(getSupportFragmentManager(), "add_task");
    }

    private void openEditDialog(Task task) {
        AddEditTaskDialogFragment dlg = AddEditTaskDialogFragment.newInstance(task);
        dlg.setOnSaveListener(edited -> {
            db.updateTask(task);
            loadTasks();
        });
        dlg.show(getSupportFragmentManager(), "edit_task");
    }

    private void loadTasks() {
        List<Task> all = db.getTasksByUser(username);
        tasks.clear();
        for (Task t : all) {
            if (!showCompleted && t.isCompleted()) continue;
            tasks.add(t);
        }
        adapter.setItems(tasks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_show_completed);
        item.setChecked(showCompleted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_completed) {
            showCompleted = !item.isChecked();
            item.setChecked(showCompleted);
            loadTasks();
            return true;
        } else if (id == R.id.action_about) {
            Snackbar.make(findViewById(R.id.rootCoordinator), "TaskMate — simple task manager", Snackbar.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_sign_out) {
            Snackbar.make(findViewById(R.id.rootCoordinator), "Sign out clicked", Snackbar.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
