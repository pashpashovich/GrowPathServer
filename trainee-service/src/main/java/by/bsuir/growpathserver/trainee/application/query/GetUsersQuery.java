package by.bsuir.growpathserver.trainee.application.query;

import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import lombok.Builder;

@Builder
public record GetUsersQuery(Integer page, Integer limit, UserRole role, UserStatus status, String search) {
}
