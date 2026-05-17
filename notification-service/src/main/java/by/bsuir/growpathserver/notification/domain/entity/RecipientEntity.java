package by.bsuir.growpathserver.notification.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import by.bsuir.growpathserver.notification.domain.valueobject.RecipientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipient")
@Getter
@Setter
@NoArgsConstructor
public class RecipientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Long id;

    @Column(name = "recipient_email", nullable = false, length = 250)
    private String email;

    @Column(name = "recipient_full_name", nullable = false, columnDefinition = "TEXT")
    private String fullName;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "recipient_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private RecipientType type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "recipient_distribution_group",
            joinColumns = @JoinColumn(name = "recipient_id"),
            inverseJoinColumns = @JoinColumn(name = "distribution_group_id")
    )
    private List<DistributionGroupEntity> distributionGroups = new ArrayList<>();
}
