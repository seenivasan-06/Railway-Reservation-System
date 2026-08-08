package railway.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Centralised input validation so DAO/service classes don't have to
 * repeat validation logic and malformed data never reaches the database.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private InputValidator() {
        // utility class - no instances
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isNullOrBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !isNullOrBlank(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidAge(int age) {
        return age > 0 && age < 130;
    }

    public static boolean isValidGender(String gender) {
        if (isNullOrBlank(gender)) {
            return false;
        }
        String g = gender.trim().toUpperCase();
        return g.equals("M") || g.equals("F") || g.equals("O");
    }

    public static boolean isValidUsername(String username) {
        return !isNullOrBlank(username) && username.trim().length() >= 3 && username.trim().length() <= 50;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Parses a date string in yyyy-MM-dd format.
     * @return the parsed LocalDate, or null if the string is invalid
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isFutureOrTodayDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
