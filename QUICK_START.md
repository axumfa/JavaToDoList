# Quick Start Guide - Task Manager

## Prerequisites

1. **Java 11+** installed
2. **PostgreSQL** installed and running
3. **Maven** (optional, but recommended)

## Step 1: Set Up PostgreSQL Database

1. **Start PostgreSQL** (if not running):
   - Windows: Check Services or run PostgreSQL service
   - macOS/Linux: `sudo service postgresql start` or `brew services start postgresql`

2. **Create the database**:
   ```bash
   psql -U postgres
   ```
   Then in PostgreSQL:
   ```sql
   CREATE DATABASE taskdb;
   \q
   ```

3. **Configure database connection** in `DatabaseConfig.java`:
   - Edit `DB_USER` and `DB_PASSWORD` if different from defaults
   - Default: `postgres` / `postgres`

## Step 2: Install Dependencies

### Option A: Using Maven (Recommended)
```bash
mvn install
```

### Option B: Manual JDBC Driver
1. Download PostgreSQL JDBC driver: https://jdbc.postgresql.org/download/
2. Add `postgresql-42.7.1.jar` to your classpath

## Step 3: Run the Server

### Using Maven:
```bash
mvn compile exec:java -Dexec.mainClass="com.example.TaskServer"
```

### Using Java directly:
```bash
# Compile all files
javac -cp ".:postgresql-42.7.1.jar" *.java

# Run server
java -cp ".:postgresql-42.7.1.jar" com.example.TaskServer
```

**Expected output:**
```
Database Configuration:
  Host: localhost
  Port: 5432
  Database: taskdb
  User: postgres
  JDBC URL: jdbc:postgresql://localhost:5432/taskdb
Database connection pool initialized with 2 connections
Database schema initialized successfully
Server listening at http://localhost:8000/tasks
```

## Step 4: Run the Client Application

### Option A: Run the demo version (with HTTP):
```bash
cd demo
mvn javafx:run
```

Or if using the root ToDoApp.java (simple version without HTTP):
```bash
javac -cp ".:javafx-sdk/lib/*" ToDoApp.java TaskManager.java Task.java
java -cp ".:javafx-sdk/lib/*" ToDoApp
```

### Option B: Run via IDE
- Right-click on `ToDoApp.java` → Run
- Or right-click on `demo/src/main/java/com/example/ToDoApp.java` → Run

## Troubleshooting

### Server won't start:
- ✅ Check PostgreSQL is running: `psql -U postgres -l`
- ✅ Verify database exists: `psql -U postgres -c "\l" | grep taskdb`
- ✅ Check credentials in `DatabaseConfig.java`

### "Connection refused" error:
- ✅ PostgreSQL service is running
- ✅ Port 5432 is not blocked by firewall
- ✅ Database `taskdb` exists

### "JDBC Driver not found":
- ✅ Run `mvn install` to download dependencies
- ✅ Or manually add PostgreSQL JDBC JAR to classpath

### Client can't connect to server:
- ✅ Server is running (check Step 3 output)
- ✅ Server shows "Server listening at http://localhost:8000/tasks"
- ✅ No firewall blocking port 8000

## Quick Test

1. **Start server** (Step 3)
2. **Start client** (Step 4)
3. **Add a task**:
   - Enter title: "Test Task"
   - Enter description: "Testing the app"
   - Select priority: "High"
   - Click "Add" or "Add Task"
4. **Verify**: Task should appear in the list

## File Structure

```
Java/
├── TaskServer.java          # HTTP Server (run this first)
├── DatabaseConfig.java      # Database configuration
├── DatabaseConnection.java  # Connection manager
├── TaskDAO.java            # Database operations
├── ToDoApp.java            # Simple client (no HTTP)
└── demo/
    └── src/main/java/com/example/
        ├── ToDoApp.java     # Full client (with HTTP)
        └── TaskManager.java # HTTP client manager
```

## Summary

**Terminal 1 - Server:**
```bash
mvn compile exec:java -Dexec.mainClass="com.example.TaskServer"
```

**Terminal 2 - Client:**
```bash
cd demo
mvn javafx:run
```

That's it! 🚀

