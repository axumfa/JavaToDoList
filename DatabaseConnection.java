package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Database Connection Manager
 * 
 * Manages PostgreSQL database connections with a simple connection pool.
 * This class handles connection creation, pooling, and cleanup.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private final BlockingQueue<Connection> connectionPool;
    private final int poolSize;
    
    private DatabaseConnection() throws SQLException {
        this.poolSize = DatabaseConfig.getMaxPoolSize();
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
        initializePool();
    }
    
    /**
     * Gets the singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     * @throws SQLException if connection initialization fails
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Initializes the connection pool with database connections
     * @throws SQLException if connection creation fails
     */
    private void initializePool() throws SQLException {
        // Load PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found. Make sure postgresql.jar is in the classpath.", e);
        }
        
        // Create initial connections
        for (int i = 0; i < DatabaseConfig.getMinPoolSize(); i++) {
            Connection conn = createConnection();
            connectionPool.offer(conn);
        }
        
        System.out.println("Database connection pool initialized with " + DatabaseConfig.getMinPoolSize() + " connections");
    }
    
    /**
     * Creates a new database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
            DatabaseConfig.getJdbcUrl(),
            DatabaseConfig.getUsername(),
            DatabaseConfig.getPassword()
        );
        // Set auto-commit to true for simplicity
        conn.setAutoCommit(true);
        return conn;
    }
    
    /**
     * Gets a connection from the pool
     * Creates a new connection if pool is empty and under max size
     * @return Connection object
     * @throws SQLException if connection creation fails
     */
    public Connection getConnection() throws SQLException {
        Connection conn = connectionPool.poll();
        if (conn == null || conn.isClosed()) {
            conn = createConnection();
        }
        return conn;
    }
    
    /**
     * Returns a connection to the pool
     * @param conn Connection to return
     */
    public void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && connectionPool.size() < poolSize) {
                    connectionPool.offer(conn);
                } else if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error returning connection to pool: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes all connections in the pool
     */
    public void closeAll() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tests the database connection
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

