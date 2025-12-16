package by.bsuir.growpathserver.notification.domain.aggregate;

import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateEntity;
import lombok.Getter;

@Getter
public class EmailTemplate {
    private final Long id;
    private final String name;
    private final String subject;
    private final String body;

    private EmailTemplate(EmailTemplateEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.subject = entity.getSubject();
        this.body = entity.getBody();
    }

    public static EmailTemplate fromEntity(EmailTemplateEntity entity) {
        return new EmailTemplate(entity);
    }

    public EmailTemplateEntity toEntity() {
        EmailTemplateEntity entity = new EmailTemplateEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setSubject(this.subject);
        entity.setBody(this.body);
        return entity;
    }

    public static EmailTemplate create(String name, String subject, String body) {
        EmailTemplateEntity entity = new EmailTemplateEntity();
        entity.setName(name);
        entity.setSubject(subject);
        entity.setBody(body);
        return new EmailTemplate(entity);
    }
}
