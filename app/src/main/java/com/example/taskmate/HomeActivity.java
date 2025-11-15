package com.example.taskmate;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) {
            overflow = DrawableCompat.wrap(overflow).mutate();
            DrawableCompat.setTint(overflow, ContextCompat.getColor(this, R.color.white));
            toolbar.setOverflowIcon(overflow);
        }
        // find a Toolbar in the layout, set it as the app's primary app bar, and then change the color of its overflow menu icon (the "three dots") to white.
        rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(tasks, this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> openAddDialog());

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { // 0 means drag and drop is disabled
            Task recentlyDeletedTask = null;
            int recentlyDeletedPosition = -1;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }// drag and drop method

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                recentlyDeletedTask = tasks.get(pos);
                recentlyDeletedPosition = pos;

                db.deleteTask(recentlyDeletedTask.getId());
                loadTasks();

                Snackbar.make(findViewById(R.id.rootCoordinator), "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            Task t = recentlyDeletedTask;
                            db.insertTask(t.getTitle(),  t.getDueDate(), t.isCompleted(), username);
                            loadTasks();
                        }).show();
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(rv); // creates a new instance of ItemTouchHelper, configured with your custom logic defined in the simpleItemTouchCallback object and attaches to the recycler view

        // handle back press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        loadTasks(); // only executed here to show tasks
    }

    private void openAddDialog() {
        AddEditTaskDialogFragment dlg = AddEditTaskDialogFragment.newInstance(null);
        dlg.show(getSupportFragmentManager(), "add_task");
    }

    private void openEditDialog(Task task) {
        AddEditTaskDialogFragment dlg = AddEditTaskDialogFragment.newInstance(task);
        dlg.show(getSupportFragmentManager(), "edit_task");
    }

    private void loadTasks() {
        List<Task> all = db.getTasksByUser(username);
        tasks.clear();
        for (Task t : all) {
            if (!showCompleted && t.isCompleted()) continue;
            tasks.add(t);
        }
        adapter.setItems(tasks);//better than notifyDatasetChanged
    }
// overflow menu is rooted in android system that's why the methods are found in the parent class "AppCompatActivity"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);//show the menu resource
        MenuItem item = menu.findItem(R.id.action_show_completed);
        item.setChecked(showCompleted);// to set whether completed are shown or not
        return true;// show the menu
    }// this is called when the menu needs to be created

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_show_completed) {
            showCompleted = !item.isChecked();//because item's state doesn't change automatically, only manually
            item.setChecked(showCompleted);
            loadTasks();
            return true;
        }  else if (id == R.id.action_sign_out) {
            AtomicBoolean yes = new AtomicBoolean(false);//boolean that can run in a different thread
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {//which dialog, which button
                        // Exit the app
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        yes.set(true);
                    })
                    .setNegativeButton("No", null)
                    .show();
            if (yes.get()) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item); //if none of the options were selected, implement default behavior
    }
    public void saveTask(Task task) {
        if (task.getId() == 0) {
            db.insertTask(task.getTitle(), task.getDueDate(), false, username);
        } else {
            db.updateTask(task);
        }
        loadTasks();
    }
    public void onTaskItemClicked(Task task) {
        openEditDialog(task);
    }

    // Method to handle checkbox change
    public void onTaskItemChecked(Task task, boolean isChecked) {
        db.updateTaskCompletion(task.getId(), isChecked);
        if (isChecked)
        {Snackbar.make(findViewById(R.id.rootCoordinator), "Task Completed", Snackbar.LENGTH_LONG).show();}
        loadTasks();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();//cleanup memory, etc
        db.close();
    }
}
