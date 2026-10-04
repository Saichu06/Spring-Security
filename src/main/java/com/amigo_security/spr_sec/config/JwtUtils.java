package com.amigo_security.spr_sec.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    private static final String JWT_SECRET =
            "VGhpc0lzQVNlY3JldEtleUZvckpXVEFuZFByb2RlY3Rpb24xMjM0NTY3ODkw";

    // =========================
    // Extract Username
    // =========================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // =========================
    // Extract Claim
    // =========================

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =========================
    // Generate Token
    // =========================

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, Map.of());
    }

    public String generateToken(
            UserDetails userDetails,
            Map<String, Object> claims
    ) {
        return createToken(claims, userDetails);
    }

    // =========================
    // Create Token
    // =========================

    private String createToken(
            Map<String, Object> claims,
            UserDetails userDetails
    ) {

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())

                // Authorities
                .claim(
                        "authorities",
                        userDetails.getAuthorities()
                )

                // Issued At
                .issuedAt(
                        new Date(System.currentTimeMillis())
                )

                // Expiration - 24 hours
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60 * 24
                        )
                )

                // Sign JWT
                .signWith(getSignInKey())

                // Build JWT
                .compact();
    }

    // =========================
    // Validate Token
    // =========================

    public Boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // =========================
    // Check Expiration
    // =========================

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =========================
    // Extract Expiration
    // =========================

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =========================
    // Extract All Claims
    // =========================

    private Claims extractAllClaims(String token) {

        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================
    // Signing Key
    // =========================

    private SecretKey getSignInKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(JWT_SECRET);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}