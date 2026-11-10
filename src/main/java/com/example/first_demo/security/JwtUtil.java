
package com.example.first_demo.security;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey123";

    private static final Key key =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 15 minutes
    private static final long ACCESS_TOKEN_EXPIRY =
            15 * 60 * 1000;

    // 7 days
    private static final long REFRESH_TOKEN_EXPIRY =
            7 * 24 * 60 * 60 * 1000;

    // Generate Access Token
    public static String generateAccessToken(String username) {

        Date now = new Date();

        return Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + ACCESS_TOKEN_EXPIRY)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate Refresh Token
    public static String generateRefreshToken(String username) {

        Date now = new Date();

        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(
                        new Date(now.getTime() + REFRESH_TOKEN_EXPIRY)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract and validate claims
    private static Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate Access Token
    public static String validateAccessToken(String token) {

        Claims claims = getClaims(token);

        if (!"access".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid access token");
        }

        return claims.getSubject();
    }

    // Validate Refresh Token
    public static String validateRefreshToken(String token) {

        Claims claims = getClaims(token);

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new RuntimeException("Invalid refresh token");
        }

        return claims.getSubject();
    }

    // Your old method (optional backward compatibility)
    public static String validateToken(String token) {
        return getClaims(token).getSubject();
    }
}
