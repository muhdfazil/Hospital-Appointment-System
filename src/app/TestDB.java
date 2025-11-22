package app;
import java.sql.*;
public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hospitaldb?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String user = "root";            // <- set exactly as in DBConnection.java
        String pass = "root";            // <- set exactly as in DBConnection.java
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected OK: " + c.getMetaData().getDatabaseProductName() + " " + c.getMetaData().getDatabaseProductVersion());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
