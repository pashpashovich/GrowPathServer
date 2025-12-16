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
@Table(name = "email_template_attachment")
@Getter
@Setter
@NoArgsConstructor
public class EmailTemplateAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_template_attachment_id")
    private Long id;

    @Column(name = "email_template_id", nullable = false)
    private Long emailTemplateId;

    @Column(name = "email_template_attachment_name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "email_template_attachment_token", nullable = false, columnDefinition = "TEXT")
    private String token;
}
