package com.example.taskmate;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class DBHelper extends SQLiteOpenHelper{
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TASKS = "tasks";

    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_HASHED = "hashed_password";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_DUE = "due_date";
    public static final String COL_TASK_COMPLETED = "is_completed";
    public static final String COL_TASK_USERNAME = "username";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public DBHelper(Context context) {
        super(context, "liudb", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_USER_USERNAME + " TEXT PRIMARY KEY, "
                + COL_USER_HASHED + " TEXT NOT NULL"
                + ");";
        db.execSQL(sqlUsers);
        String sqlTasks = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_DUE + " TEXT, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + COL_TASK_USERNAME + " TEXT NOT NULL"
                + ");";
        db.execSQL(sqlTasks);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }// in case of upgrading the db to a new version
    public long registerUser(User user) {
        if (userExists(user.getUsername())) {
            return -1;
        }
        return insertUser(user);
    }
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();//read-only
        Cursor c = null;
        try {
            c = db.query(TABLE_USERS, new String[]{COL_USER_USERNAME},
                    COL_USER_USERNAME + " = ?", new String[]{username},
                    null, null, null, "1");//table name, columns, where clause, where clause args, group by, having, order by, limit
            return (c != null && c.moveToFirst());
        } finally {
            if (c != null) c.close();//close the cursor to prevent memory leak
        }
    }
    public boolean validateUser(User user) {
        String storedHash = getHashedPassword(user.getUsername());
        return storedHash != null && storedHash.equals(user.getPassword());
    }
    public String getHashedPassword(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(TABLE_USERS, new String[]{COL_USER_HASHED},
                    COL_USER_USERNAME + " = ?", new String[]{username},
                    null, null, null, "1");
            if (c != null && c.moveToFirst()) {
                return c.getString(0);
            } else {
                return null;
            }
        } finally {
            if (c != null) c.close();
        }
    }
    private long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();// read and write
        ContentValues cv = new ContentValues();// store key-value pairs that can be processed by the db object
        cv.put(COL_USER_USERNAME, user.getUsername());
        cv.put(COL_USER_HASHED, user.getPassword());
        return db.insert(TABLE_USERS, null, cv); //If the ContentValues object cv is empty, this parameter specifies a column name where a NULL value will be explicitly inserted. In this code, it's set to null, meaning no row will be inserted if cv is empty.
    }
    public long insertTask(String title,  LocalDateTime dueDate, boolean isCompleted, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DUE, dueDate == null ? null : dueDate.format(ISO_FMT));
        cv.put(COL_TASK_COMPLETED, isCompleted ? 1 : 0);
        cv.put(COL_TASK_USERNAME, username);
        return db.insert(TABLE_TASKS, null, cv);
    }

    public List<Task> getTasksByUser(String username) {
        List<Task> list = new ArrayList<>();// in case you wanted to change from arraylist to another type
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(TABLE_TASKS,
                    null, // select all columns
                    COL_TASK_USERNAME + " = ?",
                    new String[]{username},
                    null, null,
                    COL_TASK_DUE + " ASC NULLS LAST");
            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow(COL_TASK_ID));// throws an exception
                    String title = c.getString(c.getColumnIndexOrThrow(COL_TASK_TITLE));
                    String dueText = c.getString(c.getColumnIndexOrThrow(COL_TASK_DUE));
                    LocalDateTime due = dueText == null ? null : LocalDateTime.parse(dueText, ISO_FMT);
                    boolean completed = c.getInt(c.getColumnIndexOrThrow(COL_TASK_COMPLETED)) != 0;
                    String u = c.getString(c.getColumnIndexOrThrow(COL_TASK_USERNAME));
                    list.add(new Task(id, title, due, completed, u));
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }
        return list;
    }


    public int updateTaskCompletion(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, isCompleted ? 1 : 0);
        return db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public int deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, task.getTitle());
        cv.put(COL_TASK_DUE, task.getDueDate() == null ? null : task.getDueDate().format(ISO_FMT));
        return db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});

    }

    @Override
    public synchronized void close() {
        super.close();
    }// synchronized means only one thread can execute this method on a particular instance at a time, close all connections (overriden just to make sure)
}