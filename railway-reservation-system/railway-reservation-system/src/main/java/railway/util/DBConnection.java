package railway.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC connections to the railway_reservation MySQL database.
 * <p>
 * Update DB_URL / DB_USER / DB_PASSWORD to match your local MySQL setup,
 * or supply them as JVM system properties / environment variables:
 * -Ddb.url=... -Ddb.user=... -Ddb.password=...
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/railway_reservation?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static final String DB_URL = System.getProperty("db.url",
            System.getenv().getOrDefault("DB_URL", DEFAULT_URL));
    private static final String DB_USER = System.getProperty("db.user",
            System.getenv().getOrDefault("DB_USER", DEFAULT_USER));
    private static final String DB_PASSWORD = System.getProperty("db.password",
            System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD));

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL JDBC driver not found on classpath: " + e.getMessage());
        }
    }

    private DBConnection() {
        // utility class - no instances
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
