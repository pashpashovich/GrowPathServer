package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.Email;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import lombok.Getter;

@Getter
public class User {
    private final Long id;
    private final Email email;
    private final String firstName;
    private final String lastName;
    private final String patronymicName;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private final Long invitedBy;
    private final LocalDateTime invitationSentAt;

    private User(UserEntity entity) {
        this.id = entity.getId();
        this.email = new Email(entity.getEmail());
        this.role = entity.getRole();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.lastLogin = entity.getLastLogin();
        this.invitedBy = entity.getInvitedBy();
        this.invitationSentAt = entity.getInvitationSentAt();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.patronymicName = entity.getPatronymicName();
    }

    public static User fromEntity(UserEntity entity) {
        return new User(entity);
    }

    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(this.id);
        entity.setEmail(this.email.value());
        entity.setFirstName(this.firstName);
        entity.setLastName(this.lastName);
        entity.setPatronymicName(this.patronymicName);
        entity.setRole(this.role);
        entity.setStatus(this.status);
        entity.setCreatedAt(this.createdAt);
        entity.setLastLogin(this.lastLogin);
        entity.setInvitedBy(this.invitedBy);
        entity.setInvitationSentAt(this.invitationSentAt);
        return entity;
    }

    public static User create(String email,
                              String firstName,
                              String lastName,
                              String patronymicName,
                              UserRole role,
                              Long invitedBy) {
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setPatronymicName(patronymicName);
        entity.setRole(role);
        entity.setStatus(UserStatus.PENDING);
        entity.setInvitedBy(invitedBy);
        entity.setInvitationSentAt(LocalDateTime.now());
        return fromEntity(entity);
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Single-line display for lists, ratings, and legacy API fields that still expose {@code name}.
     */
    public String getDisplayName() {
        return Stream.of(firstName, lastName, patronymicName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
    }
}
