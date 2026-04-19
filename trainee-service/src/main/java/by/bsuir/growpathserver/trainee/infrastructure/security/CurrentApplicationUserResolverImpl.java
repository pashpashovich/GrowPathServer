package by.bsuir.growpathserver.trainee.infrastructure.security;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.common.util.JwtUtils;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentApplicationUserResolverImpl implements CurrentApplicationUserResolver {

    private final UserRepository userRepository;

    @Override
    public Optional<Long> resolveCurrentUserDatabaseId() {
        Optional<Jwt> jwtOpt = extractJwt(SecurityContextHolder.getContext().getAuthentication());
        if (jwtOpt.isEmpty()) {
            return Optional.empty();
        }
        Jwt jwt = jwtOpt.get();

        String sub = jwt.getSubject();
        if (StringUtils.isNotBlank(sub)) {
            Optional<Long> byKeycloak = userRepository.findByKeycloakUserId(sub.trim()).map(UserEntity::getId);
            if (byKeycloak.isPresent()) {
                return byKeycloak;
            }
        }

        String email = firstNonBlank(JwtUtils.getEmail(jwt), JwtUtils.getUsername(jwt));
        if (StringUtils.isNotBlank(email)) {
            return userRepository.findByEmailIgnoreCase(email.trim()).map(UserEntity::getId);
        }
        return Optional.empty();
    }

    private static Optional<Jwt> extractJwt(Authentication auth) {
        if (auth == null) {
            return Optional.empty();
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.isNotBlank(a)) {
            return a;
        }
        if (StringUtils.isNotBlank(b)) {
            return b;
        }
        return null;
    }
}
