package com.skyworld.repository;

import com.skyworld.domain.enums.TokenType;
import com.skyworld.util.db.ConnectionPool;
import com.skyworld.util.security.Crypto;
import com.skyworld.util.security.Encryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.security.SecureRandom;
import java.util.HexFormat;

public class TokenRepository {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    public Long getUserIdByToken(
            TokenType type,
            String rawToken
    ) throws SQLException {

        String tokenHash = Encryption.SHA256(rawToken);

        String sql = """
                SELECT user_account_id
                FROM access_tokens
                WHERE access_token_value_hash = ?
                  AND access_token_type = ?::access_token_type
                  AND access_token_revoked_at IS NULL
                  AND access_token_expires_at > NOW()
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenHash);
            ps.setString(2, type.name());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getLong("user_account_id")
                        : null;
            }
        }
    }

    public String issueToken(
            long userAccountId,
            TokenType type,
            Duration validFor,
            int tokenLength
    ) throws SQLException {

        String rawToken = generateRawToken(tokenLength);
        String tokenHash = Encryption.SHA256(rawToken);

        String sql = """
                INSERT INTO access_tokens (
                    user_account_id,
                    access_token_type,
                    access_token_value_hash,
                    access_token_expires_at
                )
                VALUES (
                    ?,
                    ?::access_token_type,
                    ?,
                    NOW() + ?::interval
                )
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userAccountId);
            ps.setString(2, type.name());
            ps.setString(3, tokenHash);
            ps.setString(
                    4,
                    validFor.toSeconds() + " seconds"
            );

            ps.executeUpdate();
        }

        return rawToken;
    }

    public void revokeToken(String rawToken) throws SQLException {

        String tokenHash = Encryption.SHA256(rawToken);

        String sql = """
                UPDATE access_tokens
                SET access_token_revoked_at = NOW()
                WHERE access_token_value_hash = ?
                  AND access_token_revoked_at IS NULL
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tokenHash);
            ps.executeUpdate();
        }
    }

    private static String generateRawToken(int tokenLength) {

        if (tokenLength <= 0) {
            throw new IllegalArgumentException(
                    "Token length must be greater than zero"
            );
        }

        /*
         * Hexadecimal encoding produces two characters
         * for every random byte.
         */
        int byteLength = (tokenLength + 1) / 2;

        byte[] randomBytes = new byte[byteLength];

        SECURE_RANDOM.nextBytes(randomBytes);

        String token =
                HexFormat.of().formatHex(randomBytes);

        return token.substring(0, tokenLength);
    }
}