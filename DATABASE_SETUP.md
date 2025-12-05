# PostgreSQL Database Setup Guide

This guide will help you set up PostgreSQL for the Task Manager application.

## Prerequisites

1. **PostgreSQL installed** on your system
   - Download from: https://www.postgresql.org/download/
   - Or use a package manager:
     - Windows: `choco install postgresql`
     - macOS: `brew install postgresql`
     - Linux: `sudo apt-get install postgresql`

2. **PostgreSQL JDBC Driver**
   - The driver will be added via Maven dependency (see below)

## Step 1: Create PostgreSQL Database

1. Open PostgreSQL command line or pgAdmin
2. Connect to PostgreSQL server
3. Create a new database:

```sql
CREATE DATABASE taskdb;
```

Or using command line:
```bash
psql -U postgres
CREATE DATABASE taskdb;
\q
```

## Step 2: Configure Database Connection

Edit `DatabaseConfig.java` and update the following values according to your PostgreSQL setup:

```java
private static final String DB_HOST = "localhost";      // Your PostgreSQL host
private static final String DB_PORT = "5432";           // Your PostgreSQL port (default: 5432)
private static final String DB_NAME = "taskdb";          // Database name
private static final String DB_USER = "postgres";        // Your PostgreSQL username
private static final String DB_PASSWORD = "postgres";    // Your PostgreSQL password
```

## Step 3: Add PostgreSQL JDBC Dependency

If you're using Maven, add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

If you're not using Maven, download the PostgreSQL JDBC driver JAR file from:
https://jdbc.postgresql.org/download/

And add it to your project's classpath.

## Step 4: Run the Application

1. **Start PostgreSQL service** (if not already running):
   - Windows: Check Services or run `pg_ctl start`
   - macOS/Linux: `sudo service postgresql start` or `brew services start postgresql`

2. **Run TaskServer**:
   ```bash
   javac -cp ".:postgresql-42.7.1.jar" *.java
   java -cp ".:postgresql-42.7.1.jar" com.example.TaskServer
   ```

   Or if using Maven:
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.example.TaskServer"
   ```

3. The server will automatically create the `tasks` table on first run.

## Database Schema

The application automatically creates the following table:

```sql
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) DEFAULT 'Medium',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Troubleshooting

### Connection Refused Error
- Make sure PostgreSQL is running
- Check if the port (default 5432) is correct
- Verify firewall settings

### Authentication Failed
- Check username and password in `DatabaseConfig.java`
- Verify PostgreSQL user permissions

### Database Not Found
- Make sure the database `taskdb` exists
- Create it using: `CREATE DATABASE taskdb;`

### JDBC Driver Not Found
- Ensure PostgreSQL JDBC driver is in the classpath
- Check Maven dependencies if using Maven

## Files Created

- **DatabaseConfig.java** - Database configuration settings
- **DatabaseConnection.java** - Connection pool manager
- **TaskDAO.java** - Data Access Object for task operations
- **TaskServer.java** - Updated to use PostgreSQL instead of in-memory storage

## Testing the Connection

The server will print database configuration and test the connection on startup. If you see:
```
Database Configuration:
  Host: localhost
  Port: 5432
  Database: taskdb
  User: postgres
  JDBC URL: jdbc:postgresql://localhost:5432/taskdb
Database connection pool initialized with 2 connections
Database schema initialized successfully
```

Then your database connection is working correctly!

