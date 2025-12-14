package by.bsuir.growpathserver.trainee.domain.events;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

public record UserCreatedEvent(String userId, String email, String name, UserRole role, String invitedBy,
                               LocalDateTime createdAt) {
}
