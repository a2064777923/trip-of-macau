package com.aoxiaoyou.tripofmacau.common.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    public String generateAccessToken(Long userId, String openId, String nickname) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("openId", openId)
                .withClaim("nickname", nickname)
                .withClaim("tokenType", "access")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessTokenValidity)))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(verify(token).getSubject());
    }

    public String getOpenId(String token) {
        return verify(token).getClaim("openId").asString();
    }

    public String getNickname(String token) {
        return verify(token).getClaim("nickname").asString();
    }
}
