package railway.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates unique PNR (Passenger Name Record) numbers.
 * <p>
 * Format: PNR + yyMMdd + 4 random digits, e.g. PNR2606081234
 * The timestamp component keeps PNRs roughly sortable by booking date,
 * while the random suffix keeps collisions extremely unlikely. The DAO
 * layer additionally verifies uniqueness against the database before
 * committing a booking.
 */
public final class PNRGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    private PNRGenerator() {
        // utility class - no instances
    }

    public static String generate() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        int randomPart = 1000 + RANDOM.nextInt(9000); // 4-digit random number
        return "PNR" + datePart + randomPart;
    }
}
