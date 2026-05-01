package com.thex.chat.chatapi.security;

import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenValidatorTest {

    private static final String SECRET = "TestSecretKeyAtLeast256BitsLong0123456789ABCDEF";
    private static final String OTHER_SECRET = "AnotherDifferentSecretKey0123456789ABCDEFGHIJKL";

    private final JwtTokenValidator validator = new JwtTokenValidator(SECRET);

    @Test
    void validateToken_returnsTrue_forFreshSignedToken() {
        String token = signedCurrent(SECRET, 1, "fresh", Instant.now().plusSeconds(60));

        assertThat(validator.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forExpiredToken() {
        String token = signedCurrent(SECRET, 2, "expired", Instant.now().minusSeconds(60));

        assertThat(validator.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forWrongSignature() {
        String token = signedCurrent(OTHER_SECRET, 3, "wrong-sig", Instant.now().plusSeconds(60));

        assertThat(validator.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forMalformedToken() {
        assertThat(validator.validateToken("not-a-jwt")).isFalse();
        assertThat(validator.validateToken("")).isFalse();
    }

    @Test
    void getUserFromToken_readsIdFromSubjectAndUsernameFromClaim() {
        String token = signedCurrent(SECRET, 100, "bob", Instant.now().plusSeconds(60));

        UserInfo user = validator.getUserFromToken(token);

        assertThat(user).isEqualTo(new UserInfo("100", "bob", UserType.REGISTERED));
    }

    @Test
    void getUserFromToken_throws_whenSignatureInvalid() {
        String token = signedCurrent(OTHER_SECRET, 5, "invalid-sig", Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> validator.getUserFromToken(token))
            .isInstanceOf(JwtException.class);
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds a token in the current format: subject = userId, username carried as a claim.
     */
    private static String signedCurrent(String secret, Integer userId, String username, Instant expiry) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .expiration(Date.from(expiry))
            .signWith(key(secret))
            .compact();
    }
}
