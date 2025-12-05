package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskManager {
    // Point this to server; server default below uses port 8000
    private static final String BASE_URL = "http://localhost:8000/tasks";

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final HttpClient http = HttpClient.newHttpClient();

    public ObservableList<Task> getTasks() { return tasks; }

    public void fetchAll() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            ArrayList<Task> list = tasksFromJson(resp.body());
            tasks.setAll(list);
        } else {
            throw new IOException("Fetch failed: " + resp.statusCode());
        }
    }

    public Task addTask(Task t) throws IOException, InterruptedException {
        String json = taskToJson(t, false);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            Task created = singleTaskFromJson(resp.body());
            if (created != null) tasks.add(created);
            return created;
        } else {
            throw new IOException("Add failed: " + resp.statusCode() + " " + resp.body());
        }
    }

    public Task updateTask(Task t) throws IOException, InterruptedException {
        if (t.getId() == null) throw new IllegalArgumentException("ID required");
        String json = taskToJson(t, true);
        String url = BASE_URL + "/" + t.getId();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            Task updated = singleTaskFromJson(resp.body());
            if (updated != null) {
                for (int i = 0; i < tasks.size(); i++) {
                    Task cur = tasks.get(i);
                    if (cur.getId() != null && cur.getId().equals(updated.getId())) {
                        tasks.set(i, updated);
                        break;
                    }
                }
            }
            return updated;
        } else {
            throw new IOException("Update failed: " + resp.statusCode() + " " + resp.body());
        }
    }

    public void removeTask(Task t) throws IOException, InterruptedException {
        if (t.getId() == null) {
            tasks.remove(t);
            return;
        }
        String url = BASE_URL + "/" + t.getId();
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            tasks.remove(t);
        } else {
            throw new IOException("Delete failed: " + resp.statusCode() + " " + resp.body());
        }
    }

    // ----- minimal JSON helpers (no external libs) -----
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String taskToJson(Task t, boolean includeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (includeId && t.getId() != null) {
            sb.append("\"id\":\"").append(escape(t.getId())).append("\",");
        }
        sb.append("\"title\":\"").append(escape(t.getTitle())).append("\",");
        sb.append("\"description\":\"").append(escape(t.getDescription())).append("\",");
        sb.append("\"priority\":\"").append(escape(t.getPriority())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private ArrayList<Task> tasksFromJson(String json) {
        ArrayList<Task> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        Pattern objPattern = Pattern.compile("\\{[^}]*\\}");
        Matcher m = objPattern.matcher(json);
        while (m.find()) {
            String obj = m.group();
            Task t = parseTaskObject(obj);
            if (t != null) list.add(t);
        }
        return list;
    }

    private Task singleTaskFromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        Pattern objPattern = Pattern.compile("\\{[^}]*\\}");
        Matcher m = objPattern.matcher(json);
        if (m.find()) return parseTaskObject(m.group());
        return null;
    }

    private Task parseTaskObject(String obj) {
        String id = getStringField(obj, "id");
        String title = getStringField(obj, "title");
        String description = getStringField(obj, "description");
        String priority = getStringField(obj, "priority");
        return new Task(id, title, description, priority);
    }

    private String getStringField(String obj, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(obj);
        if (m.find()) {
            String s = m.group(1);
            return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }
}