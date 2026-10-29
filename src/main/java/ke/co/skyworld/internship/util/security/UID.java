package ke.co.skyworld.internship.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;


public class UID {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final String salt = "abcdefghijklmnopqrstuvwxyz@#$!%^&*(*)1234567890";

    public static String generateToken(int length) {
        if (length <= 32) {
            throw new IllegalArgumentException("Length must be greater than 32");
        }

        int byteLength = (int) Math.ceil(length * 0.75);

        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        return token.substring(0, Math.min(length, token.length()));
    }

    public static String generateOtp(int length) {
        if (length <= 0) throw new IllegalArgumentException("Length must be greater than 1");

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public long getCurrentUnixTimestamp() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    public String byteToHexString(byte[] input) {
        StringBuilder output = new StringBuilder();
        for (byte anInput : input) {
            output.append(String.format("%02X", anInput));
        }
        return output.toString();
    }

    public String getSSAUUID() {
        try {
            String value = getCurrentUnixTimestamp() + salt + getUUID();
            MessageDigest md = MessageDigest.getInstance("SHA");
            return byteToHexString(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
