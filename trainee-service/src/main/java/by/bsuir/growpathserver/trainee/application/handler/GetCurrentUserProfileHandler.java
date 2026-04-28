package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetCurrentUserProfileQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetCurrentUserProfileHandler {

    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final UserRepository userRepository;

    public User handle(GetCurrentUserProfileQuery query) {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return User.fromEntity(userEntity);
    }
}
