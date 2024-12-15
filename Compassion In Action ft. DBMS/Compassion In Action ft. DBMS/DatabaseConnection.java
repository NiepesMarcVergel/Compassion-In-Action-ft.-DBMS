import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/compassion_in_action"; // the database schema
    private static final String USER = "root"; // replaced with mysql
    private static final String PASSWORD = "Mansanas1909"; // replaced with mysql password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}