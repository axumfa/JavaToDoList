# Task Manager Application

A Java-based Task Manager with PostgreSQL backend and JavaFX client.

## Project Structure

```
Java/
├── TaskServer.java          # HTTP Server (PostgreSQL backend)
├── DatabaseConfig.java      # Database configuration
├── DatabaseConnection.java  # Connection pool manager
├── TaskDAO.java            # Database operations
├── Task.java               # Task model (root - simple version)
├── postgresql-42.7.8.jar   # PostgreSQL JDBC driver
└── demo/
    └── src/main/java/com/example/
        ├── ToDoApp.java     # Main client application (USE THIS)
        ├── TaskManager.java # HTTP client manager
        └── Task.java        # Task model (with ID support)
```

## Quick Start

### 1. Set Up Database
```sql
CREATE DATABASE taskdb;
```

### 2. Configure Database
Edit `DatabaseConfig.java` with your PostgreSQL credentials.

### 3. Start Server
```powershell
cd D:\project\Java
javac -d . -cp ".;postgresql-42.7.8.jar" TaskServer.java DatabaseConfig.java DatabaseConnection.java TaskDAO.java Task.java
java -cp ".;postgresql-42.7.8.jar" com.example.TaskServer
```

### 4. Start Client
Run `demo/src/main/java/com/example/ToDoApp.java` from your IDE.

## Features

- ✅ Add tasks
- ✅ Update tasks
- ✅ Delete tasks
- ✅ View task details
- ✅ Refresh from server
- ✅ Persistent storage in PostgreSQL

## Requirements

- Java 11+
- PostgreSQL
- JavaFX (handled by IDE or Maven)

