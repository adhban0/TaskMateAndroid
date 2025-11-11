package com.example.taskmate;

import java.time.LocalDateTime;

public class Task {
    private int id;
    private String title;
    private LocalDateTime dueDate;
    private boolean isCompleted;
    private String username;

    public Task(int id, String title, LocalDateTime dueDate,boolean isCompleted, String username) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.username = username;
        this.isCompleted = isCompleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void markCompleted() {
        isCompleted = true;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
