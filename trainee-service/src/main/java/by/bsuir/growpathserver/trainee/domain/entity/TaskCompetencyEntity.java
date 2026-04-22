package by.bsuir.growpathserver.trainee.domain.entity;

import org.hibernate.annotations.SoftDelete;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "task_competencies",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_task_competency_active",
                columnNames = { "task_id", "competency_id" }
        )
)
@SoftDelete
@Getter
@Setter
@NoArgsConstructor
public class TaskCompetencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private CompetencyEntity competency;
}
