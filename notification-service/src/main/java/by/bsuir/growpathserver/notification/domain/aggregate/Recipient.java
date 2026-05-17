package by.bsuir.growpathserver.notification.domain.aggregate;

import java.util.List;

import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.RecipientEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.RecipientType;
import lombok.Getter;

@Getter
public class Recipient {
    private final Long id;
    private final String email;
    private final String fullName;
    private final Long userId;
    private final RecipientType type;
    private final List<Long> distributionGroupIds;

    private Recipient(Long id,
                      String email,
                      String fullName,
                      Long userId,
                      RecipientType type,
                      List<Long> distributionGroupIds) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userId = userId;
        this.type = type;
        this.distributionGroupIds = distributionGroupIds;
    }

    public static Recipient fromEntity(RecipientEntity entity) {
        List<Long> groupIds = entity.getDistributionGroups() == null
                ? List.of()
                : entity.getDistributionGroups().stream().map(DistributionGroupEntity::getId).toList();
        return new Recipient(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getUserId(),
                entity.getType(),
                groupIds
        );
    }

    public static Recipient create(String email, String fullName, Long userId, RecipientType type) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Recipient full name is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Recipient type is required");
        }
        return new Recipient(null, email.trim(), fullName.trim(), userId, type, List.of());
    }

    public RecipientEntity toEntity() {
        RecipientEntity entity = new RecipientEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setFullName(fullName);
        entity.setUserId(userId);
        entity.setType(type);
        return entity;
    }
}
