package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetInternByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternByIdHandler {

    private final GetUserByIdHandler getUserByIdHandler;

    public User handle(GetInternByIdQuery query) {
        try {
            Long userId = Long.parseLong(query.internId());
            GetUserByIdQuery getUserQuery = new GetUserByIdQuery(userId);
            User user = getUserByIdHandler.handle(getUserQuery);

            if (user.getRole() != UserRole.INTERN) {
                throw new IllegalArgumentException("User is not an intern");
            }

            return user;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid intern ID format: " + query.internId());
        }
    }
}
