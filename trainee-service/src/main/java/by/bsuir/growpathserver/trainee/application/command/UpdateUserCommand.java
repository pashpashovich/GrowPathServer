package by.bsuir.growpathserver.trainee.application.command;

import java.util.Optional;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

public record UpdateUserCommand(
        Long userId,
        Optional<String> email,
        Optional<String> firstName,
        Optional<String> lastName,
        Optional<String> patronymicName,
        Optional<UserRole> role
) {
}
