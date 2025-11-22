package app;

import java.sql.*;

public class DBSeed {


    public static void seedIfNeeded() {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM users")) {

            int userCount = 0;
            if (rs.next()) userCount = rs.getInt("cnt");
            if (userCount > 0) {
                // DB already has users => assumes already seeded
                System.out.println("DBSeed: skipping, DB already has data (users count=" + userCount + ")");
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // If users table missing or other error
        }

        // proceeds to insert sample data in a transaction
        String insertPatientSql = "INSERT INTO patients (name, age, gender, phone, address) VALUES (?, ?, ?, ?, ?)";
        String insertDoctorSql = "INSERT INTO doctors (name, specialization, phone) VALUES (?, ?, ?)";
        String insertApptSql = "INSERT INTO appointments (patient_id, doctor_id, date, time, symptoms) VALUES (?, ?, ?, ?, ?)";
        String insertUserSql = "INSERT INTO users (username, password, role, patient_ref_id) VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (
                PreparedStatement psPatient = c.prepareStatement(insertPatientSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psDoctor  = c.prepareStatement(insertDoctorSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psAppt    = c.prepareStatement(insertApptSql);
                PreparedStatement psUser    = c.prepareStatement(insertUserSql)
            ) {
                // --- Patients ---
                psPatient.setString(1, "Armaan Khandelwal");
                psPatient.setInt(2, 26);
                psPatient.setString(3, "Male");
                psPatient.setString(4, "9991122334");
                psPatient.setString(5, "Bhopal");
                psPatient.executeUpdate();
                int pid1 = getGeneratedId(psPatient);

                psPatient.setString(1, "Zoya Rahman");
                psPatient.setInt(2, 19);
                psPatient.setString(3, "Female");
                psPatient.setString(4, "8887766554");
                psPatient.setString(5, "Bhopal");
                psPatient.executeUpdate();
                int pid2 = getGeneratedId(psPatient);

                psPatient.setString(1, "Ajay Kumar");
                psPatient.setInt(2, 33);
                psPatient.setString(3, "Male");
                psPatient.setString(4, "7878787878");
                psPatient.setString(5, "Indore");
                psPatient.executeUpdate();
                int pid3 = getGeneratedId(psPatient);

                // --- Doctors ---
                psDoctor.setString(1, "Dr. Ayesha Khan");
                psDoctor.setString(2, "General Physician");
                psDoctor.setString(3, "9876543210");
                psDoctor.executeUpdate();
                int did1 = getGeneratedId(psDoctor);

                psDoctor.setString(1, "Dr. Prashant Yadav");
                psDoctor.setString(2, "Cardiologist");
                psDoctor.setString(3, "9123456780");
                psDoctor.executeUpdate();
                int did2 = getGeneratedId(psDoctor);

                psDoctor.setString(1, "Dr. Neha Sharma");
                psDoctor.setString(2, "Dermatologist");
                psDoctor.setString(3, "9988776655");
                psDoctor.executeUpdate();
                int did3 = getGeneratedId(psDoctor);

                // --- Appointments ---
                psAppt.setInt(1, pid1); psAppt.setInt(2, did2); psAppt.setString(3, "2025-02-15"); psAppt.setString(4, "10:30 AM"); psAppt.setString(5, "Chest pain"); psAppt.executeUpdate();
                psAppt.setInt(1, pid2); psAppt.setInt(2, did3); psAppt.setString(3, "2025-02-17"); psAppt.setString(4, "04:45 PM"); psAppt.setString(5, "Skin allergy"); psAppt.executeUpdate();
                psAppt.setInt(1, pid2); psAppt.setInt(2, did1); psAppt.setString(3, "2025-02-18"); psAppt.setString(4, "11:15 AM"); psAppt.setString(5, "Fever & cough"); psAppt.executeUpdate();

                // --- Users (default credentials for demo) ---
                psUser.setString(1, "admin");
                psUser.setString(2, "admin123"); // plaintext for demo only
                psUser.setString(3, "admin");
                psUser.setNull(4, Types.INTEGER);
                psUser.executeUpdate();

                psUser.setString(1, "reception");
                psUser.setString(2, "recep123");
                psUser.setString(3, "receptionist");
                psUser.setNull(4, Types.INTEGER);
                psUser.executeUpdate();

                // optionally created a patient user and linked patient_ref_id
                psUser.setString(1, "p_arman");
                psUser.setString(2, "p123");
                psUser.setString(3, "patient");
                psUser.setInt(4, pid1);
                psUser.executeUpdate();

                c.commit();
                System.out.println("DBSeed: sample data inserted successfully.");
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
                System.err.println("DBSeed: rollback due to error.");
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static int getGeneratedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys != null && keys.next()) return keys.getInt(1);
        }
        // fallback if driver doesn't return generated keys
        return -1;
    }
}
