package app;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    // parse date stored as YYYY-MM-DD or other common formats
    public static LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String[] patterns = {"yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(p));
            } catch (DateTimeParseException e) { /* try next */ }
        }
        
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    // parse time strings like "10:30 AM", "10:30", "15:30"
    public static LocalTime parseTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String[] patterns = {"h:mm a", "hh:mm a", "H:mm", "HH:mm", "h:mm"};
        for (String p : patterns) {
            try {
                return LocalTime.parse(s, DateTimeFormatter.ofPattern(p));
            } catch (DateTimeParseException e) { /* try next */ }
        }
        // last resort try ISO
        try { return LocalTime.parse(s); } catch (Exception e) { return null; }
    }

    // Formatters for storage (recommended formats)
    public static final DateTimeFormatter STORE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter STORE_TIME = DateTimeFormatter.ofPattern("HH:mm");
}
