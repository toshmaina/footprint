package ke.co.skyworld.internship.util.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Encryption {


    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 32;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String base64Encode(String clearText) {
        return Base64.getEncoder().withoutPadding().encodeToString(clearText.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Encode(Byte[] bytes) {
        return Base64.getEncoder().withoutPadding().encodeToString(ByteUtils.toByteArray(bytes));
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String base64Decode(String hash) {
        return new String(Base64.getDecoder().decode(hash), StandardCharsets.UTF_8);
    }

    public static Byte[] base64DecodeToBytes(String hash) {
        return ByteUtils.toByteObjectArray(Base64.getDecoder().decode(hash));
    }

    public static byte[] base64DecodeTo_bytes(String hash) {
        return Base64.getDecoder().decode(hash);
    }

    public static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public static String SHA256(String base) {
        return Crypto.hash("SHA-256", base);
    }

    public static String MD5(String base) {
        return Crypto.hash("MD5", base);
    }

    public static String maskHashedPassword(String clearText) {
        return mask(clearText, '*', 4, 8);
    }

    public static String mask(String clearText, char maskChar, int charsToExpose, int maskCount) {
        String mask = String.valueOf(maskChar).repeat(maskCount);
        final Pattern hidePattern = Pattern.compile("(.{" + charsToExpose + "})(.*)(.{" + charsToExpose + "})");
        Matcher m = hidePattern.matcher(clearText);
        if (m.find()) {
            return m.group(1) + mask + m.group(3);
        }
        return clearText;
    }

    public static String hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            byte[] hash = deriveKey(password, salt);

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);

            return saltB64 + "$" + hashB64;
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            byte[] computedHash = deriveKey(password, salt);

            return constantTimeEquals(storedHashBytes, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] deriveKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }
}

