package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBConnection {

    // LOCALAPPDATA on Windows; fallback to user.home
    private static Path getAppDataBaseDir() {
        String localApp = System.getenv("LOCALAPPDATA");
        if (localApp != null && !localApp.trim().isEmpty()) {
            return Paths.get(localApp, "HospitalAppointmentSystem");
        } else {
            // fallback (cross-platform)
            String userHome = System.getProperty("user.home");
            return Paths.get(userHome, "AppData", "Local", "HospitalAppointmentSystem");
        }
    }

    // compute actual sqlite URL at runtime
    private static String getSqliteUrl() {
        Path dbFile = getAppDataBaseDir().resolve("db").resolve("hospital.db");
        // SQLite JDBC expected file path like jdbc:sqlite:C:/path/to/hospital.db
        return "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace("\\", "/");
    }

    // ---- GET CONNECTION ----
    public static Connection getConnection() throws SQLException {

        // ensures parent folders exist
        try {
            Path base = getAppDataBaseDir();
            Path dbDir = base.resolve("db");
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
                System.out.println("DBConnection: created folder " + dbDir.toAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Could not create DB folder: " + e.getMessage(), e);
        }

        // ensures JDBC driver loaded 
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // will fail on getConnection if jar missing
        }

        String sqliteUrl = getSqliteUrl();
        Connection conn = DriverManager.getConnection(sqliteUrl);

        // enable foreign keys
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException ex) {
            // ignore if not supported
        }
        return conn;
    }

    // ---- CREATES TABLES AUTOMATICALLY IF THEY DO NOT EXIST ----
    public static void initializeIfNeeded() {
        String[] ddl = new String[] {
            "CREATE TABLE IF NOT EXISTS patients ("
                + "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, age INTEGER, gender TEXT, phone TEXT, address TEXT)",
            "CREATE TABLE IF NOT EXISTS doctors ("
                + "doctor_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, specialization TEXT, phone TEXT)",
            "CREATE TABLE IF NOT EXISTS appointments ("
                + "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "patient_id INTEGER NOT NULL, doctor_id INTEGER NOT NULL, "
                + "date TEXT NOT NULL, time TEXT, symptoms TEXT, "
                + "FOREIGN KEY(patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE, "
                + "FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE)",
            "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, "
                + "role TEXT NOT NULL, patient_ref_id INTEGER)"
        };

        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            for (String s : ddl) st.execute(s);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
