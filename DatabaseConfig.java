package com.example;

/**
 * PostgreSQL Database Configuration
 * 
 * This class holds all database connection configuration settings.
 * You can modify these values according to your PostgreSQL setup.
 */
public class DatabaseConfig {
    
    // Database connection parameters
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "taskdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "5858";
    
    // Connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_POOL_SIZE = 2;
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    /**
     * Constructs the JDBC URL for PostgreSQL connection
     * @return JDBC connection URL
     */
    public static String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
    }
    
    /**
     * Gets the database username
     * @return database username
     */
    public static String getUsername() {
        return DB_USER;
    }
    
    /**
     * Gets the database password
     * @return database password
     */
    public static String getPassword() {
        return DB_PASSWORD;
    }
    
    /**
     * Gets the maximum pool size for connection pooling
     * @return maximum pool size
     */
    public static int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }
    
    /**
     * Gets the minimum pool size for connection pooling
     * @return minimum pool size
     */
    public static int getMinPoolSize() {
        return MIN_POOL_SIZE;
    }
    
    /**
     * Gets the connection timeout in milliseconds
     * @return connection timeout
     */
    public static int getConnectionTimeout() {
        return CONNECTION_TIMEOUT;
    }
    
    /**
     * Prints current database configuration (without password)
     */
    public static void printConfig() {
        System.out.println("Database Configuration:");
        System.out.println("  Host: " + DB_HOST);
        System.out.println("  Port: " + DB_PORT);
        System.out.println("  Database: " + DB_NAME);
        System.out.println("  User: " + DB_USER);
        System.out.println("  JDBC URL: " + getJdbcUrl());
    }
}

