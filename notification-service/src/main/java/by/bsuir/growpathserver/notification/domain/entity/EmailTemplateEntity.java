package by.bsuir.growpathserver.notification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_template")
@Getter
@Setter
@NoArgsConstructor
public class EmailTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_template_id")
    private Long id;

    @Column(name = "email_template_name", nullable = false, length = 512)
    private String name;

    @Column(name = "email_template_subject", nullable = false, length = 512)
    private String subject;

    @Column(name = "email_template_body", nullable = false, columnDefinition = "TEXT")
    private String body;
}
