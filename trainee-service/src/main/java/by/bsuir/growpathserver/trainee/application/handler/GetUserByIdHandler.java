package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetUserByIdHandler {

    private final UserRepository userRepository;

    public User handle(GetUserByIdQuery query) {
        UserEntity entity = userRepository.findById(query.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + query.userId()));
        return User.fromEntity(entity);
    }
}
