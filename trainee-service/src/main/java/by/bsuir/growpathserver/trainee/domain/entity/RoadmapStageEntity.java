package by.bsuir.growpathserver.trainee.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapStageStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.StagePriorityLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roadmap_stages")
@Getter
@Setter
@NoArgsConstructor
public class RoadmapStageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private RoadmapEntity roadmap;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoadmapStageStatus status = RoadmapStageStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private StagePriorityLevel priority;

    @Column(name = "is_checkpoint", nullable = false)
    private boolean checkpoint;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (Objects.isNull(createdAt)) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
