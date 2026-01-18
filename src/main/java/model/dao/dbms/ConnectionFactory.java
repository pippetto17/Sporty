package model.dao.dbms;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private ConnectionFactory() {
        // Private constructor per impedire istanziazione
    }

    private static final Properties properties;

    static {
        try (InputStream input = ConnectionFactory.class.getResourceAsStream("/db.properties")) {
            properties = new Properties();
            if (input == null) {
                throw new IOException("File db.properties not found in resources.");
            }
            properties.load(input);

            // Carica il driver JDBC
            String driver = properties.getProperty("db.driver");
            if (driver != null) {
                Class.forName(driver);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Error loading database properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new SQLException("Database connection properties are not properly configured.");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
