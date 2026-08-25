package ke.co.skyworld.internship.service;

import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.domain.enums.TokenType;
import ke.co.skyworld.internship.repository.TokenRepository;

import java.sql.SQLException;
import java.time.Duration;

public class TokenService {

    private static final Duration ACCESS_TOKEN_DURATION =
            createDuration(
                    Constants.getAccessTokenTimeout(),
                    Constants.getAccessTokenTimeoutUnit()
            );
    private static final int REFRESH_TOKEN_LENGTH = Constants.getRefreshTokenLength();
    private static final int ACCESS_TOKEN_LENGTH = Constants.getAccessTokenLength();

    private static final Duration REFRESH_TOKEN_DURATION =
            createDuration(
                    Constants.getRefreshTokenTimeout(),
                    Constants.getRefreshTokenTimeoutUnit()
            );
    private final TokenRepository tokenRepository = new TokenRepository();

    public TokenService() {

    }

    public String issueAccessToken(long userAccountId) throws SQLException {
        validateUserId(userAccountId);

        return tokenRepository.issueToken(
                userAccountId,
                TokenType.ACCESS,
                ACCESS_TOKEN_DURATION,
                ACCESS_TOKEN_LENGTH
        );
    }

    public String issueRefreshToken(long userAccountId) throws SQLException {
        validateUserId(userAccountId);

        return tokenRepository.issueToken(
                userAccountId,
                TokenType.REFRESH,
                REFRESH_TOKEN_DURATION,
                REFRESH_TOKEN_LENGTH
        );
    }

    public Long authenticateAccessToken(String rawToken)
            throws SQLException {

        return authenticate(TokenType.ACCESS, rawToken);
    }

    public Long authenticateRefreshToken(String rawToken)
            throws SQLException {

        return authenticate(TokenType.REFRESH, rawToken);
    }

    private Long authenticate(
            TokenType tokenType,
            String rawToken
    ) throws SQLException {

        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }

        return tokenRepository.getUserIdByToken(
                tokenType,
                rawToken
        );
    }

    public void revokeToken(String rawToken) throws SQLException {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Token cannot be null or blank"
            );
        }

        tokenRepository.revokeToken(rawToken);
    }

    private void validateUserId(long userAccountId) {
        if (userAccountId <= 0) {
            throw new IllegalArgumentException(
                    "User account ID must be greater than zero"
            );
        }

    }
    private static Duration createDuration(int timeout, String unit) {

        if (timeout <= 0) {
            throw new IllegalArgumentException(
                    "Token timeout must be greater than zero"
            );
        }

        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(
                    "Token timeout unit cannot be null or blank"
            );
        }

        return switch (unit.trim().toLowerCase()) {
            case "seconds", "second", "sec", "s" ->
                    Duration.ofSeconds(timeout);

            case "minutes", "minute", "min", "m" ->
                    Duration.ofMinutes(timeout);

            case "hours", "hour", "hr", "h" ->
                    Duration.ofHours(timeout);

            case "days", "day", "d" ->
                    Duration.ofDays(timeout);

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported token timeout unit: " + unit
                    );
        };
    }
}