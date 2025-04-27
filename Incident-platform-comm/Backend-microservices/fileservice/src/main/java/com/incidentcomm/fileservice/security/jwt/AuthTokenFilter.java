package com.incidentcomm.fileservice.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = jwtUtils.parseJwt(request);
            Long userId = null;
            List<String> roles = List.of();

            // First try to get userId from the token
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                userId = jwtUtils.getUserIdFromJwtToken(jwt);
                roles = jwtUtils.getRolesFromJwtToken(jwt);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // If userId is still null, try to get it from X-User-ID header
            if (userId == null) {
                String userIdHeader = request.getHeader("X-User-ID");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        userId = Long.parseLong(userIdHeader);
                        log.info("Using userId from X-User-ID header: {}", userId);
                    } catch (NumberFormatException e) {
                        log.error("Invalid X-User-ID header value: {}", userIdHeader);
                    }
                }
            }

            // Finally, if we have a userId, set it in the request attributes
            if (userId != null) {
                request.setAttribute("userId", userId);
                // Also set it as a header for downstream services
                response.setHeader("X-User-ID", userId.toString());
            } else {
                // For testing purposes, use a default userId of 1
                userId = 1L;
                request.setAttribute("userId", userId);
                log.warn("Using default userId: 1. This should only happen in development.");
            }

            // Set user roles as an attribute for authorization checks
            request.setAttribute("userRoles", roles);

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}