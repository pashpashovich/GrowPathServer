package by.bsuir.growpathserver.trainee.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.converter.InternshipProgramStatusConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "internship_programs")
@Getter
@Setter
@NoArgsConstructor
public class InternshipProgramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "max_places", nullable = false)
    private Integer maxPlaces;

    @Column(name = "it_direction")
    private String itDirection;

    @OneToMany(mappedBy = "internshipProgram", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<InternshipProgramRequirementEntity> requirementItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "internshipProgram", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<InternshipProgramGoalEntity> goalItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "internshipProgram", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private Set<InternshipProgramSelectionStageEntity> selectionStageItems = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "internship_program_competencies",
            joinColumns = @JoinColumn(name = "internship_program_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id")
    )
    private Set<CompetencyEntity> competencies = new HashSet<>();

    @Column(nullable = false)
    @Convert(converter = InternshipProgramStatusConverter.class)
    private InternshipProgramStatus status = InternshipProgramStatus.DRAFT;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String internships;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
