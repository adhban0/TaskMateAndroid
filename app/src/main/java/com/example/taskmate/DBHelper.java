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
    private static final String DB_NAME = "liudb";
    private static final int DB_VERSION = 1;
    public static final String TABLE_USERS = "users";
    public static final String TABLE_TASKS = "tasks";

    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_HASHED = "hashed_password";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_DESCRIPTION = "description";
    public static final String COL_TASK_DUE = "due_date";          
    public static final String COL_TASK_COMPLETED = "is_completed";
    public static final String COL_TASK_USERNAME = "username";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_USER_USERNAME + " TEXT PRIMARY KEY, "
                + COL_USER_HASHED + " TEXT NOT NULL"
                + ");";
        db.execSQL(sqlUsers);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    public long registerUser(User user) {
        if (userExists(user.getUsername())) {
            return -1;
        }
        return insertUser(user);
    }
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(TABLE_USERS, new String[]{COL_USER_USERNAME},
                    COL_USER_USERNAME + " = ?", new String[]{username},
                    null, null, null, "1");
            return (c != null && c.moveToFirst());
        } finally {
            if (c != null) c.close();
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_USERNAME, user.getUsername());
        cv.put(COL_USER_HASHED, user.getPassword());
        return db.insert(TABLE_USERS, null, cv);
    }
    public long insertTask(String title, String description, LocalDateTime dueDate, boolean isCompleted, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, title);
        cv.put(COL_TASK_DESCRIPTION, description);
        cv.put(COL_TASK_DUE, dueDate == null ? null : dueDate.format(ISO_FMT));
        cv.put(COL_TASK_COMPLETED, isCompleted ? 1 : 0);
        cv.put(COL_TASK_USERNAME, username);
        return db.insert(TABLE_TASKS, null, cv);
    }

    public List<Task> getTasksByUser(String username) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(TABLE_TASKS,
                    null,
                    COL_TASK_USERNAME + " = ?",
                    new String[]{username},
                    null, null,
                    COL_TASK_DUE + " ASC");
            if (c != null && c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow(COL_TASK_ID));
                    String title = c.getString(c.getColumnIndexOrThrow(COL_TASK_TITLE));
                    String desc = c.getString(c.getColumnIndexOrThrow(COL_TASK_DESCRIPTION));
                    String dueText = c.getString(c.getColumnIndexOrThrow(COL_TASK_DUE));
                    LocalDateTime due = dueText == null ? null : LocalDateTime.parse(dueText, ISO_FMT);
                    boolean completed = c.getInt(c.getColumnIndexOrThrow(COL_TASK_COMPLETED)) != 0;
                    String u = c.getString(c.getColumnIndexOrThrow(COL_TASK_USERNAME));
                    list.add(new Task(id, title, desc, due, completed, u));
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

    @Override
    public synchronized void close() {
        super.close();
    }
}