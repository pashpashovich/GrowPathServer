package by.bsuir.growpathserver.trainee.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "internship_program_requirements")
@Getter
@Setter
@NoArgsConstructor
public class InternshipProgramRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_program_id", nullable = false)
    private InternshipProgramEntity internshipProgram;

    @Column(name = "requirement_text", nullable = false, columnDefinition = "TEXT")
    private String requirementText;
}
