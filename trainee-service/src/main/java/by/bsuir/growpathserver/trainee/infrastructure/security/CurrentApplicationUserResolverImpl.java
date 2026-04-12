package by.bsuir.growpathserver.trainee.infrastructure.security;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        Jwt jwt = jwtAuth.getToken();
        String sub = jwt.getSubject();
        if (StringUtils.isNotBlank(sub)) {
            Optional<Long> byKeycloak = userRepository.findByKeycloakUserId(sub).map(UserEntity::getId);
            if (byKeycloak.isPresent()) {
                return byKeycloak;
            }
        }
        String email = firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("preferred_username"));
        if (StringUtils.isNotBlank(email)) {
            return userRepository.findByEmail(email).map(UserEntity::getId);
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
