package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.ChangeUserRoleCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateInternCommand;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateInternHandler {

    private final GetUserByIdHandler getUserByIdHandler;
    private final ChangeUserRoleHandler changeUserRoleHandler;

    public User handle(CreateInternCommand command) {
        try {
            Long userId = Long.parseLong(command.userId());
            GetUserByIdQuery getUserQuery = new GetUserByIdQuery(userId);
            User existingUser = getUserByIdHandler.handle(getUserQuery);

            if (existingUser.getRole() != UserRole.INTERN) {
                ChangeUserRoleCommand changeRoleCommand = ChangeUserRoleCommand.builder()
                        .userId(userId)
                        .role(UserRole.INTERN)
                        .build();
                return changeUserRoleHandler.handle(changeRoleCommand);
            }

            return existingUser;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + command.userId());
        }
    }
}
