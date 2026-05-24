package by.bsuir.growpathserver.trainee.application.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MentorQueryScopeResolver {

    private final CurrentApplicationUserResolver currentUserResolver;

    public Long resolveOptionalMentorFilter(Long requestedMentorId) {
        Long currentUserId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        if (isMentor() && !isHrOrAdmin()) {
            if (Objects.isNull(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            if (Objects.nonNull(requestedMentorId) && !Objects.equals(requestedMentorId, currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                  "Mentor can only access their own program data");
            }
            return currentUserId;
        }
        return requestedMentorId;
    }

    private boolean isHrOrAdmin() {
        return hasAuthority("HR_MANAGER") || hasAuthority("ADMIN");
    }

    private boolean isMentor() {
        return hasAuthority("MENTOR");
    }

    private static boolean hasAuthority(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth)) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
