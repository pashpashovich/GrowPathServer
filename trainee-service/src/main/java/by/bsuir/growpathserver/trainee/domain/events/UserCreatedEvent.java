package by.bsuir.growpathserver.trainee.domain.events;

import java.time.OffsetDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

public record UserCreatedEvent(String userId, String email, String name, UserRole role, String invitedBy,
                               OffsetDateTime createdAt) {
}
