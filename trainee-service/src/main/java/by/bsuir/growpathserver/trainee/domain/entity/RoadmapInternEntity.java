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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "roadmap_interns",
        uniqueConstraints = @UniqueConstraint(name = "uq_roadmap_intern", columnNames = { "roadmap_id",
                "keycloak_user_id" })
)
@Getter
@Setter
@NoArgsConstructor
public class RoadmapInternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private RoadmapEntity roadmap;

    @Column(name = "keycloak_user_id", nullable = false, length = 64)
    private String keycloakUserId;
}
