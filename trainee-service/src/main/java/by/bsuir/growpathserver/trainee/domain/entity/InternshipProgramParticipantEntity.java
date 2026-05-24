package by.bsuir.growpathserver.trainee.domain.entity;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.valueobject.ProgramParticipantRole;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "internship_program_participants")
@Getter
@Setter
@NoArgsConstructor
public class InternshipProgramParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "internship_program_id", nullable = false)
    private InternshipProgramEntity program;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramParticipantRole role;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private UserEntity mentor;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void onAssign() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
