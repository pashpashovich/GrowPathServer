package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.application.service.UserService;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateUserHandler {

    private final UserService userService;

    public User handle(CreateUserCommand command) {
        return userService.createUser(command);
    }
}
