package by.bsuir.growpathserver.trainee.domain.entity;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "intern_hiring_decisions")
@Getter
@Setter
@NoArgsConstructor
public class InternHiringDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "intern_id", nullable = false)
    private UserEntity intern;

    @ManyToOne(optional = false)
    @JoinColumn(name = "internship_program_id", nullable = false)
    private InternshipProgramEntity program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HiringDecisionType decision;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "decided_by_user_id", nullable = false)
    private UserEntity decidedBy;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (decidedAt == null) {
            decidedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
