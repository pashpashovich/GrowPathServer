package by.bsuir.growpathserver.trainee.infrastructure.security;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import by.bsuir.growpathserver.trainee.application.service.LastLoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LastLoginUpdateFilter extends OncePerRequestFilter {

    private final LastLoginService lastLoginService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String sub = jwt.getSubject();
            String email = Optional.ofNullable(jwt.getClaimAsString("email"))
                    .filter(StringUtils::isNotBlank)
                    .orElse(StringUtils.trimToNull(jwt.getClaimAsString("preferred_username")));
            lastLoginService.recordIfStale(sub, email);
        }
        filterChain.doFilter(request, response);
    }
}
