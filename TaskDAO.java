package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Task operations
 * 
 * Handles all database operations for tasks including CRUD operations.
 */
public class TaskDAO {
    
    private final DatabaseConnection dbConnection;
    
    public TaskDAO() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Initializes the database schema (creates tasks table if it doesn't exist)
     * @throws SQLException if schema creation fails
     */
    public void initializeSchema() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS tasks (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                priority VARCHAR(50) DEFAULT 'Medium',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Database schema initialized successfully");
        }
    }
    
    /**
     * Retrieves all tasks from the database
     * @return List of task maps
     * @throws SQLException if query fails
     */
    public List<Map<String, String>> getAllTasks() throws SQLException {
        List<Map<String, String>> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, priority FROM tasks ORDER BY id";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, String> task = new LinkedHashMap<>();
                task.put("id", String.valueOf(rs.getInt("id")));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                task.put("priority", rs.getString("priority") != null ? rs.getString("priority") : "Medium");
                tasks.add(task);
            }
        }
        
        return tasks;
    }
    
    /**
     * Retrieves a single task by ID
     * @param id Task ID
     * @return Task map or null if not found
     * @throws SQLException if query fails
     */
    public Map<String, String> getTaskById(String id) throws SQLException {
        String sql = "SELECT id, title, description, priority FROM tasks WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(id));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> task = new LinkedHashMap<>();
                    task.put("id", String.valueOf(rs.getInt("id")));
                    task.put("title", rs.getString("title"));
                    task.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                    task.put("priority", rs.getString("priority") != null ? rs.getString("priority") : "Medium");
                    return task;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Creates a new task in the database
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @return Created task map with generated ID
     * @throws SQLException if insert fails
     */
    public Map<String, String> createTask(String title, String description, String priority) throws SQLException {
        String sql = "INSERT INTO tasks (title, description, priority) VALUES (?, ?, ?) RETURNING id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, description != null ? description : "");
            pstmt.setString(3, priority != null ? priority : "Medium");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    Map<String, String> task = new LinkedHashMap<>();
                    task.put("id", String.valueOf(id));
                    task.put("title", title);
                    task.put("description", description != null ? description : "");
                    task.put("priority", priority != null ? priority : "Medium");
                    return task;
                }
            }
        }
        
        throw new SQLException("Failed to create task");
    }
    
    /**
     * Updates an existing task in the database
     * @param id Task ID
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @return Updated task map or null if not found
     * @throws SQLException if update fails
     */
    public Map<String, String> updateTask(String id, String title, String description, String priority) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, priority = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? RETURNING id, title, description, priority";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, description != null ? description : "");
            pstmt.setString(3, priority != null ? priority : "Medium");
            pstmt.setInt(4, Integer.parseInt(id));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> task = new LinkedHashMap<>();
                    task.put("id", String.valueOf(rs.getInt("id")));
                    task.put("title", rs.getString("title"));
                    task.put("description", rs.getString("description") != null ? rs.getString("description") : "");
                    task.put("priority", rs.getString("priority") != null ? rs.getString("priority") : "Medium");
                    return task;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Deletes a task from the database
     * @param id Task ID
     * @return true if task was deleted, false if not found
     * @throws SQLException if delete fails
     */
    public boolean deleteTask(String id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(id));
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}

