package app;

public class Session {
    public static String currentUsername = null;
    public static String currentUserRole = "guest";
    public static int currentUserId = -1; // optional: links to users.user_id
    public static int currentPatientId = -1; // if logged-in user is a patient, stores their patient_id
}
