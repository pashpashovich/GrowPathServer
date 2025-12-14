package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.Email;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import lombok.Getter;

@Getter
public class User {
    private final String id;
    private final Email email;
    private final String name;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private final String invitedBy;
    private final LocalDateTime invitationSentAt;

    private User(UserEntity entity) {
        this.id = entity.getId();
        this.email = new Email(entity.getEmail());
        this.name = entity.getName();
        this.role = entity.getRole();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.lastLogin = entity.getLastLogin();
        this.invitedBy = entity.getInvitedBy();
        this.invitationSentAt = entity.getInvitationSentAt();
    }

    public static User fromEntity(UserEntity entity) {
        return new User(entity);
    }

    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(this.id);
        entity.setEmail(this.email.value());
        entity.setName(this.name);
        entity.setRole(this.role);
        entity.setStatus(this.status);
        entity.setCreatedAt(this.createdAt);
        entity.setLastLogin(this.lastLogin);
        entity.setInvitedBy(this.invitedBy);
        entity.setInvitationSentAt(this.invitationSentAt);
        return entity;
    }

    public static User create(String email, String name, UserRole role, String invitedBy) {
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setName(name);
        entity.setRole(role);
        entity.setStatus(UserStatus.PENDING);
        entity.setInvitedBy(invitedBy);
        entity.setInvitationSentAt(LocalDateTime.now());
        return fromEntity(entity);
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
