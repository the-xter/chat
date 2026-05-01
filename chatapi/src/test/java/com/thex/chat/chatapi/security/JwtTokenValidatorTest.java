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
        String token = signed(SECRET, "alice", 42, Instant.now().plusSeconds(60));

        assertThat(validator.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalse_forExpiredToken() {
        String token = signed(SECRET, "alice", 42, Instant.now().minusSeconds(60));

        assertThat(validator.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forWrongSignature() {
        String token = signed(OTHER_SECRET, "alice", 42, Instant.now().plusSeconds(60));

        assertThat(validator.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalse_forMalformedToken() {
        assertThat(validator.validateToken("not-a-jwt")).isFalse();
        assertThat(validator.validateToken("")).isFalse();
    }

    @Test
    void getUserFromToken_extractsSubjectAndUserIdAsRegistered() {
        String token = signed(SECRET, "alice", 42, Instant.now().plusSeconds(60));

        UserInfo user = validator.getUserFromToken(token);

        assertThat(user).isEqualTo(new UserInfo("42", "alice", UserType.REGISTERED));
    }

    @Test
    void getUserFromToken_returnsLiteralNullString_whenUserIdClaimMissing() {
        // Documents current behaviour: String.valueOf((Integer) null) yields "null".
        String token = Jwts.builder()
            .subject("alice")
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(key(SECRET))
            .compact();

        UserInfo user = validator.getUserFromToken(token);

        assertThat(user.id()).isEqualTo("null");
        assertThat(user.name()).isEqualTo("alice");
        assertThat(user.type()).isEqualTo(UserType.REGISTERED);
    }

    @Test
    void getUserFromToken_throws_whenSignatureInvalid() {
        String token = signed(OTHER_SECRET, "alice", 42, Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> validator.getUserFromToken(token))
            .isInstanceOf(JwtException.class);
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static String signed(String secret, String subject, Integer userId, Instant expiry) {
        return Jwts.builder()
            .subject(subject)
            .claim("userId", userId)
            .expiration(Date.from(expiry))
            .signWith(key(secret))
            .compact();
    }
}
