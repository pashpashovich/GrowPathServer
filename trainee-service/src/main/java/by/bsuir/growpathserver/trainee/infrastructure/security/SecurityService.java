package by.bsuir.growpathserver.trainee.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.common.util.JwtUtils;

@Component
public class SecurityService {
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new IllegalStateException("User is not authenticated or not a JWT token");
        }
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = JwtUtils.getUserId(jwt);
        if (userId == null) {
            throw new IllegalStateException("User ID not found in JWT token");
        }
        return userId;
    }

    public Long getCurrentUserIdAsLong() {
        String userId = getCurrentUserId();
        try {
            return Long.parseLong(userId);
        }
        catch (NumberFormatException e) {
            throw new IllegalStateException("User ID is not a valid Long: " + userId);
        }
    }
}
