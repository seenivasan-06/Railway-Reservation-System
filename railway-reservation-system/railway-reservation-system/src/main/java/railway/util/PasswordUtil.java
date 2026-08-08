package railway.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple SHA-256 based password hashing helper.
 * <p>
 * Note: for a real production system a salted, slow hash (e.g. BCrypt or
 * Argon2) should be used instead of a bare SHA-256 digest. SHA-256 is used
 * here (matching MySQL's SHA2() used for the seed admin account) to keep
 * the project dependency-free and easy to run for a college submission.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // utility class - no instances
    }

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matches(String plainText, String hash) {
        return hash != null && hash.equalsIgnoreCase(hash(plainText));
    }
}
