package com.example;
public class Task {
    private String id;
    private String title;
    private String description;
    private String priority;

    public Task(String title, String description, String priority) {
        this(null, title, description, priority);
    }

    public Task(String id, String title, String description, String priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }

    @Override
    public String toString() {
        String t = (title == null || title.isEmpty()) ? "(no title)" : title;
        String p = (priority == null) ? "None" : priority;
        return t + "  [" + p + "]";
    }
}
