package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class TaskServer {

    private static final int PORT = 8000;
    private static final String BASE = "/tasks";
    
    private static TaskDAO taskDAO;

    public static void main(String[] args) throws Exception {
        // Initialize database connection and schema
        try {
            DatabaseConfig.printConfig();
            taskDAO = new TaskDAO();
            taskDAO.initializeSchema();
            
            // Test database connection
            if (!DatabaseConnection.getInstance().testConnection()) {
                System.err.println("WARNING: Database connection test failed!");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to initialize database: " + e.getMessage());
            System.err.println("Please check your PostgreSQL configuration in DatabaseConfig.java");
            System.err.println("Make sure PostgreSQL is running and the database exists.");
            throw e;
        }
        
        HttpServer srv = HttpServer.create(new InetSocketAddress(PORT), 0);
        srv.createContext(BASE, new TasksHandler());
        srv.createContext(BASE + "/", new TaskItemHandler());
        srv.setExecutor(null);
        System.out.println("Server listening at http://localhost:" + PORT + BASE);
        srv.start();
        
        // Add shutdown hook to close database connections
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down... Closing database connections...");
            try {
                DatabaseConnection.getInstance().closeAll();
            } catch (SQLException e) {
                System.err.println("Error closing database connections: " + e.getMessage());
            }
        }));
    }

    static class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetAll(ex);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleCreate(ex);
                } else {
                    sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private void handleGetAll(HttpExchange ex) throws IOException {
            try {
                List<Map<String, String>> tasks = taskDAO.getAllTasks();
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                boolean first = true;
                for (Map<String, String> task : tasks) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(mapToJsonObject(task.get("id"), task));
                }
                sb.append("]");
                sendResponse(ex, 200, sb.toString());
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        }

        private void handleCreate(HttpExchange ex) throws IOException {
            try {
                String body = readBody(ex);
                Map<String,String> m = parseJsonToMap(body);
                String title = m.getOrDefault("title", "").trim();
                if (title.isEmpty()) {
                    sendResponse(ex, 400, "{\"error\":\"Title is required\"}");
                    return;
                }
                String description = m.getOrDefault("description", "");
                String priority = m.getOrDefault("priority", "Medium");
                
                Map<String, String> created = taskDAO.createTask(title, description, priority);
                String out = mapToJsonObject(created.get("id"), created);
                sendResponse(ex, 201, out);
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        }
    }

    static class TaskItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                String method = ex.getRequestMethod();
                String path = ex.getRequestURI().getPath();
                String id = path.substring(path.lastIndexOf('/') + 1);
                if (id == null || id.isEmpty()) {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                    return;
                }
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(ex, id);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    handlePut(ex, id);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDelete(ex, id);
                } else {
                    sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(ex, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private void handleGet(HttpExchange ex, String id) throws IOException {
            try {
                Map<String, String> rec = taskDAO.getTaskById(id);
                if (rec == null) {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                    return;
                }
                sendResponse(ex, 200, mapToJsonObject(id, rec));
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            } catch (NumberFormatException e) {
                sendResponse(ex, 400, "{\"error\":\"Invalid task ID\"}");
            }
        }

        private void handlePut(HttpExchange ex, String id) throws IOException {
            try {
                Map<String, String> existing = taskDAO.getTaskById(id);
                if (existing == null) {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                    return;
                }
                
                String body = readBody(ex);
                Map<String,String> m = parseJsonToMap(body);
                String title = m.getOrDefault("title", "").trim();
                if (title.isEmpty()) {
                    sendResponse(ex, 400, "{\"error\":\"Title is required\"}");
                    return;
                }
                
                String description = m.getOrDefault("description", "");
                String priority = m.getOrDefault("priority", "Medium");
                
                Map<String, String> updated = taskDAO.updateTask(id, title, description, priority);
                if (updated == null) {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                    return;
                }
                sendResponse(ex, 200, mapToJsonObject(id, updated));
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            } catch (NumberFormatException e) {
                sendResponse(ex, 400, "{\"error\":\"Invalid task ID\"}");
            }
        }

        private void handleDelete(HttpExchange ex, String id) throws IOException {
            try {
                boolean deleted = taskDAO.deleteTask(id);
                if (!deleted) {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                    return;
                }
                sendResponse(ex, 204, "");
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            } catch (NumberFormatException e) {
                sendResponse(ex, 400, "{\"error\":\"Invalid task ID\"}");
            }
        }
    }

    // --- helpers ---
    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendResponse(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String mapToJsonObject(String id, Map<String,String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escape(id)).append("\",");
        sb.append("\"title\":\"").append(escape(map.getOrDefault("title", ""))).append("\",");
        sb.append("\"description\":\"").append(escape(map.getOrDefault("description", ""))).append("\",");
        sb.append("\"priority\":\"").append(escape(map.getOrDefault("priority", "Medium"))).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static Map<String,String> parseJsonToMap(String json) {
        Map<String,String> out = new LinkedHashMap<>();
        if (json == null) return out;
        // very simple parser for flat {"k":"v", ...}
        String s = json.trim();
        s = s.replaceAll("[\\{\\}]","").trim();
        if (s.isEmpty()) return out;
        String[] parts = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String p : parts) {
            String[] kv = p.split(":",2);
            if (kv.length < 2) continue;
            String key = kv[0].trim().replaceAll("^\"|\"$", "");
            String val = kv[1].trim();
            val = val.replaceAll("^\"|\"$", "");
            val = val.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
            out.put(key, val);
        }
        return out;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}