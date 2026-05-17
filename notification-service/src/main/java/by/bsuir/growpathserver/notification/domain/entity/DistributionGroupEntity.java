package by.bsuir.growpathserver.notification.domain.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distribution_group")
@Getter
@Setter
@NoArgsConstructor
public class DistributionGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distribution_group_id")
    private Long id;

    @Column(name = "distribution_group_name", nullable = false, length = 256)
    private String name;

    @Column(name = "distribution_group_description", length = 256)
    private String description;

    @ManyToMany(mappedBy = "distributionGroups", fetch = FetchType.LAZY)
    private List<RecipientEntity> recipients = new ArrayList<>();
}
