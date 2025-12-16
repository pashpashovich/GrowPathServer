package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.entity.DepartmentEntity;
import lombok.Getter;

@Getter
public class Department {
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Department(DepartmentEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public static Department fromEntity(DepartmentEntity entity) {
        return new Department(entity);
    }

    public DepartmentEntity toEntity() {
        DepartmentEntity entity = new DepartmentEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setDescription(this.description);
        entity.setCreatedAt(this.createdAt);
        entity.setUpdatedAt(this.updatedAt);
        return entity;
    }

    public static Department create(String name, String description) {
        DepartmentEntity entity = new DepartmentEntity();
        entity.setName(name);
        entity.setDescription(description);
        return new Department(entity);
    }
}

