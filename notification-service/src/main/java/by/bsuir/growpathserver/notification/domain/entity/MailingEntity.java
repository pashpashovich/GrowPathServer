package by.bsuir.growpathserver.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mailing")
@SQLDelete(sql = "UPDATE mailing SET is_deleted = true WHERE mailing_id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class MailingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mailing_id")
    private Long id;

    @Column(name = "mailing_name", nullable = false, length = 512)
    private String name;

    @Column(name = "mailing_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private MailingType type;

    @Column(name = "mailing_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private MailingStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_template_id", nullable = false)
    private EmailTemplateEntity template;

    @Column(name = "mailing_execute_at")
    private LocalDateTime executeAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private boolean deleted;

    @OneToMany(mappedBy = "mailing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MailingScheduleEntity> schedules = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mailing_recipient_group",
            joinColumns = @JoinColumn(name = "mailing_id"),
            inverseJoinColumns = @JoinColumn(name = "distribution_group_id")
    )
    private Set<DistributionGroupEntity> distributionGroups = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
