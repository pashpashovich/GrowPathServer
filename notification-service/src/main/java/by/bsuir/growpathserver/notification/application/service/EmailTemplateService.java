package by.bsuir.growpathserver.notification.application.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.notification.application.command.CreateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplateByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplatesQuery;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateAttachmentStorageService.PresignUploadResult;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;

public interface EmailTemplateService {
    EmailTemplate createEmailTemplate(CreateEmailTemplateCommand command);

    EmailTemplate updateEmailTemplate(UpdateEmailTemplateCommand command);

    void deleteEmailTemplate(DeleteEmailTemplateCommand command);

    EmailTemplate getEmailTemplateById(GetEmailTemplateByIdQuery query);

    Page<EmailTemplate> getEmailTemplates(GetEmailTemplatesQuery query);

    PresignUploadResult presignAttachmentUpload(String fileName);

    Resource downloadAttachment(Long templateId, Long attachmentId);
}
