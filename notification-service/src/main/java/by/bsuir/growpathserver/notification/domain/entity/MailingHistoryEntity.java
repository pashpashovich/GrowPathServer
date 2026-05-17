package by.bsuir.growpathserver.notification.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mailing_history")
@Getter
@Setter
@NoArgsConstructor
public class MailingHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mailing_id", nullable = false)
    private Long mailingId;

    @Column(name = "name")
    private String name;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "mailing_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private MailingType mailingType;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
