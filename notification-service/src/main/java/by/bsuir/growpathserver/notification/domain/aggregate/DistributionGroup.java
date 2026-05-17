package by.bsuir.growpathserver.notification.domain.aggregate;

import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import lombok.Getter;

@Getter
public class DistributionGroup {
    private final Long id;
    private final String name;
    private final String description;
    private final int recipientCount;

    private DistributionGroup(Long id, String name, String description, int recipientCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.recipientCount = recipientCount;
    }

    public static DistributionGroup fromEntity(DistributionGroupEntity entity) {
        int count = entity.getRecipients() != null ? entity.getRecipients().size() : 0;
        return new DistributionGroup(entity.getId(), entity.getName(), entity.getDescription(), count);
    }

    public static DistributionGroup create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Distribution group name is required");
        }
        return new DistributionGroup(null, name.trim(), description, 0);
    }

    public DistributionGroupEntity toEntity() {
        DistributionGroupEntity entity = new DistributionGroupEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        return entity;
    }
}
