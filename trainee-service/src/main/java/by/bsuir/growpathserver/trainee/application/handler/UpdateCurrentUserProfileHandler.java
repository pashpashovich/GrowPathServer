package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.application.command.UpdateCurrentUserProfileCommand;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateCurrentUserProfileHandler {

    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final UserRepository userRepository;

    @Transactional
    public User handle(UpdateCurrentUserProfileCommand command) {
        Long userId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userEntity.setFirstName(command.getFirstName());
        userEntity.setLastName(command.getLastName());
        userEntity.setPatronymicName(command.getPatronymicName());
        userEntity.setPhoneNumber(command.getPhoneNumber());

        UserEntity savedEntity = userRepository.save(userEntity);

        return User.fromEntity(savedEntity);
    }
}
