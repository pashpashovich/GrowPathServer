package by.bsuir.growpathserver.notification.application.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.command.CreateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplateByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplatesQuery;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateAttachmentEntity;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateEntity;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateAttachmentRepository emailTemplateAttachmentRepository;

    @Override
    @Transactional
    public EmailTemplate createEmailTemplate(CreateEmailTemplateCommand command) {
        EmailTemplate template = EmailTemplate.create(
                command.name(),
                command.subject(),
                command.body()
        );

        EmailTemplateEntity entity = template.toEntity();
        EmailTemplateEntity savedEntity = emailTemplateRepository.save(entity);

        if (command.attachments() != null && !command.attachments().isEmpty()) {
            List<EmailTemplateAttachmentEntity> attachmentEntities = command.attachments().stream()
                    .map(attachment -> {
                        EmailTemplateAttachmentEntity attachmentEntity = new EmailTemplateAttachmentEntity();
                        attachmentEntity.setEmailTemplateId(savedEntity.getId());
                        attachmentEntity.setName(attachment.name());
                        attachmentEntity.setToken(attachment.token());
                        return attachmentEntity;
                    })
                    .toList();
            emailTemplateAttachmentRepository.saveAll(attachmentEntities);
        }

        return EmailTemplate.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public EmailTemplate updateEmailTemplate(UpdateEmailTemplateCommand command) {
        EmailTemplateEntity entity = emailTemplateRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Email template not found with id: " + command.id()));

        if (command.name() != null) {
            entity.setName(command.name());
        }
        if (command.subject() != null) {
            entity.setSubject(command.subject());
        }
        if (command.body() != null) {
            entity.setBody(command.body());
        }

        EmailTemplateEntity savedEntity = emailTemplateRepository.save(entity);

        // Update attachments if provided
        if (command.attachments() != null) {
            // Delete existing attachments
            emailTemplateAttachmentRepository.deleteByEmailTemplateId(savedEntity.getId());

            // Create new attachments
            if (!command.attachments().isEmpty()) {
                List<EmailTemplateAttachmentEntity> attachmentEntities = command.attachments().stream()
                        .map(attachment -> {
                            EmailTemplateAttachmentEntity attachmentEntity = new EmailTemplateAttachmentEntity();
                            attachmentEntity.setEmailTemplateId(savedEntity.getId());
                            attachmentEntity.setName(attachment.name());
                            attachmentEntity.setToken(attachment.token());
                            return attachmentEntity;
                        })
                        .toList();
                emailTemplateAttachmentRepository.saveAll(attachmentEntities);
            }
        }

        return EmailTemplate.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteEmailTemplate(DeleteEmailTemplateCommand command) {
        if (!emailTemplateRepository.existsById(command.id())) {
            throw new NoSuchElementException("Email template not found with id: " + command.id());
        }
        emailTemplateRepository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplate getEmailTemplateById(GetEmailTemplateByIdQuery query) {
        EmailTemplateEntity entity = emailTemplateRepository.findById(query.id())
                .orElseThrow(() -> new NoSuchElementException("Email template not found with id: " + query.id()));
        return EmailTemplate.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailTemplate> getEmailTemplates(GetEmailTemplatesQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Page<EmailTemplateEntity> entities = emailTemplateRepository.findAll(pageable);
        return entities.map(EmailTemplate::fromEntity);
    }
}
