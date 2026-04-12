package by.bsuir.growpathserver.trainee.domain.events;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

public record UserCreatedEvent(Long userId, String email, String firstName, String lastName, String patronymicName,
                               UserRole role,
                               Long invitedBy,
                               LocalDateTime createdAt) {
}
