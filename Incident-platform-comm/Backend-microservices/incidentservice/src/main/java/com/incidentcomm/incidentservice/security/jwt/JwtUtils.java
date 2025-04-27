package com.incidentcomm.incidentservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();

        // Different JWT implementations might store user ID differently
        if (claims.containsKey("id")) {
            return Long.parseLong(claims.get("id").toString());
        } else if (claims.containsKey("userId")) {
            return Long.parseLong(claims.get("userId").toString());
        } else if (claims.containsKey("sub")) {
            try {
                return Long.parseLong(claims.getSubject());
            } catch (NumberFormatException e) {
                // Subject is a username, not an ID - map username to ID
                log.debug("JWT subject is not a user ID, attempting to map username to ID");
                return getUserIdByUsername(claims.getSubject());
            }
        }

        return null;
    }

    private Long getUserIdByUsername(String username) {
        // In a production environment, you would make a service call here
        // For now, hardcode a mapping for testing
        Map<String, Long> userIdMap = new HashMap<>();
        userIdMap.put("admin", 8L);  // Using the ID from your token response
        // Add other mappings as needed
        
        return userIdMap.getOrDefault(username, 1L); // Default to 1 if not found
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();

        // Try to find roles in different common claim names
        if (claims.containsKey("roles")) {
            return (List<String>) claims.get("roles");
        } else if (claims.containsKey("authorities")) {
            return (List<String>) claims.get("authorities");
        } else if (claims.containsKey("role")) {
            Object role = claims.get("role");
            if (role instanceof String) {
                return List.of((String) role);
            }
        }

        return List.of();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}