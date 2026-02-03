package model.dao.dbms;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private ConnectionFactory() {
    }

    private static final Properties properties;
    static {
        try (InputStream input = ConnectionFactory.class.getResourceAsStream("/db.properties")) {
            properties = new Properties();
            if (input == null) {
                throw new IOException("File db.properties not found in resources.");
            }
            properties.load(input);
            String driver = properties.getProperty("db.driver");
            if (driver != null) {
                Class.forName(driver);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error loading database properties: " + e.getMessage());
        }
    }
    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");
            if (url == null || user == null || password == null) {
                throw new SQLException("Database connection properties are not properly configured.");
            }
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}