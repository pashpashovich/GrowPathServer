package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.UpdateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateUserHandler {

    private final UserRepository userRepository;

    @Transactional
    public User handle(UpdateUserCommand command) {
        UserEntity entity = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + command.userId()));

        if (command.email() != null && !command.email().equals(entity.getEmail())) {
            if (userRepository.existsByEmail(command.email())) {
                throw new IllegalArgumentException("User with email " + command.email() + " already exists");
            }
            entity.setEmail(command.email());
        }

        if (command.name() != null) {
            entity.setName(command.name());
        }

        if (command.role() != null) {
            entity.setRole(command.role());
        }

        UserEntity savedEntity = userRepository.save(entity);
        return User.fromEntity(savedEntity);
    }
}
