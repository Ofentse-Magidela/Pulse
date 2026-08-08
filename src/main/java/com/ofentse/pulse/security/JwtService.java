package com.ofentse.pulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId) {

        long EXPIRATION_TIME = 1000 * 60 * 60;
        Date TOKEN_EXPIRATION = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(TOKEN_EXPIRATION)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUserId(String jwtToken) {
        Claims claimsMap = extractAllClaims(jwtToken);
        return claimsMap.getSubject();
    }

    private Claims extractAllClaims(String jwtToken) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }
}
