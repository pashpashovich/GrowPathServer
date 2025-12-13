package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.application.command.CreateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;

public interface UserService {
    User createUser(CreateUserCommand command);
}
