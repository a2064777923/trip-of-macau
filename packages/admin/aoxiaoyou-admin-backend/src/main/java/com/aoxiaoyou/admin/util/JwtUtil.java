package com.aoxiaoyou.admin.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withClaim("roles", roles)
                .withClaim("tokenType", "access")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessTokenValidity)))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(Long userId, String username) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withClaim("tokenType", "refresh")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(refreshTokenValidity)))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(verify(token).getSubject());
    }

    public String getUsername(String token) {
        return verify(token).getClaim("username").asString();
    }

    public List<String> getRoles(String token) {
        return verify(token).getClaim("roles").asList(String.class);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(verify(token).getClaim("tokenType").asString());
    }
}
