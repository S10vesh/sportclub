package ru.sportclub.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        this.url = env("SPORTCLUB_DB_URL", "jdbc:postgresql://localhost:5432/sportclub");
        this.user = env("SPORTCLUB_DB_USER", "postgres");
        this.password = env("SPORTCLUB_DB_PASSWORD", "postgres");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
