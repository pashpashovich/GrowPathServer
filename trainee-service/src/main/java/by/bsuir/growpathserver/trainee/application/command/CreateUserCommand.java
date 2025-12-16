package by.bsuir.growpathserver.trainee.application.command;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import lombok.Builder;

@Builder
public record CreateUserCommand(String email, String name, UserRole role, Long invitedBy) {
}
