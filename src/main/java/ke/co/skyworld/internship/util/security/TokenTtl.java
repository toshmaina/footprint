package ke.co.skyworld.internship.util.security;

import ke.co.skyworld.internship.config.Constants;

import java.time.Duration;

public final class TokenTtl {

    private TokenTtl() {
    }

    public static Duration accessTokenTtl() {
        return resolve(Constants.getAccessTokenTimeout(), Constants.getAccessTokenTimeoutUnit());
    }

    public static Duration refreshTokenTtl() {
        return resolve(Constants.getRefreshTokenTimeout(), Constants.getRefreshTokenTimeoutUnit());
    }

    private static Duration resolve(int amount, String unit) {
        return switch (unit.toLowerCase()) {
            case "seconds" -> Duration.ofSeconds(amount);
            case "minutes" -> Duration.ofMinutes(amount);
            case "hours" -> Duration.ofHours(amount);
            case "days" -> Duration.ofDays(amount);
            default -> throw new IllegalStateException("Unknown token timeout unit: " + unit);
        };
    }
}