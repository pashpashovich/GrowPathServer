package by.bsuir.growpathserver.notification.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.CreateEmailTemplateRequest;
import by.bsuir.growpathserver.dto.model.EmailTemplateAttachmentRequest;
import by.bsuir.growpathserver.dto.model.EmailTemplateListResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateResponse;
import by.bsuir.growpathserver.dto.model.PresignEmailTemplateAttachmentUploadRequest;
import by.bsuir.growpathserver.dto.model.PresignEmailTemplateAttachmentUploadResponse;
import by.bsuir.growpathserver.dto.model.UpdateEmailTemplateRequest;
import by.bsuir.growpathserver.notification.application.command.CreateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.handler.CreateEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.handler.DeleteEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.handler.GetEmailTemplateByIdHandler;
import by.bsuir.growpathserver.notification.application.handler.GetEmailTemplatesHandler;
import by.bsuir.growpathserver.notification.application.handler.UpdateEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplateByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplatesQuery;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateAttachmentStorageService.PresignUploadResult;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import by.bsuir.growpathserver.notification.infrastructure.mapper.EmailTemplateMapper;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailTemplateApplicationFacade {

    private final CreateEmailTemplateHandler createEmailTemplateHandler;
    private final GetEmailTemplateByIdHandler getEmailTemplateByIdHandler;
    private final GetEmailTemplatesHandler getEmailTemplatesHandler;
    private final UpdateEmailTemplateHandler updateEmailTemplateHandler;
    private final DeleteEmailTemplateHandler deleteEmailTemplateHandler;
    private final EmailTemplateService emailTemplateService;
    private final EmailTemplateMapper emailTemplateMapper;
    private final EmailTemplateAttachmentRepository emailTemplateAttachmentRepository;
    private final EmailTemplateAttachmentStorageService attachmentStorageService;

    public EmailTemplateResponse createEmailTemplate(CreateEmailTemplateRequest request) {
        EmailTemplate template = createEmailTemplateHandler.handle(CreateEmailTemplateCommand.builder()
                                                                           .name(request.getName())
                                                                           .subject(request.getSubject())
                                                                           .body(request.getBody())
                                                                           .attachments(mapCreateAttachments(
                                                                                   request.getAttachments()))
                                                                           .build());
        return toResponse(template);
    }

    public void deleteEmailTemplate(String id) {
        deleteEmailTemplateHandler.handle(new DeleteEmailTemplateCommand(NotificationIds.parseRequired(id, "id")));
    }

    public ResponseEntity<Resource> downloadEmailTemplateAttachment(String templateId, String attachmentId) {
        Resource resource = emailTemplateService.downloadAttachment(
                NotificationIds.parseRequired(templateId, "templateId"),
                NotificationIds.parseRequired(attachmentId, "attachmentId"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    public EmailTemplateResponse getEmailTemplateById(String id) {
        EmailTemplate template = getEmailTemplateByIdHandler.handle(
                new GetEmailTemplateByIdQuery(NotificationIds.parseRequired(id, "id")));
        return toResponse(template);
    }

    public EmailTemplateListResponse getEmailTemplates(Integer page, Integer limit) {
        Page<EmailTemplate> templatesPage = getEmailTemplatesHandler.handle(GetEmailTemplatesQuery.builder()
                                                                                    .page(page)
                                                                                    .limit(limit)
                                                                                    .build());
        return emailTemplateMapper.toEmailTemplateListResponse(
                templatesPage,
                emailTemplateAttachmentRepository,
                attachmentStorageService);
    }

    public PresignEmailTemplateAttachmentUploadResponse presignEmailTemplateAttachmentUpload(
            PresignEmailTemplateAttachmentUploadRequest request) {
        String fileName = request != null ? request.getFileName() : null;
        PresignUploadResult result = emailTemplateService.presignAttachmentUpload(fileName);
        PresignEmailTemplateAttachmentUploadResponse response = new PresignEmailTemplateAttachmentUploadResponse();
        response.setObjectKey(result.objectKey());
        response.setUploadUrl(result.uploadUrl());
        return response;
    }

    public EmailTemplateResponse updateEmailTemplate(String id, UpdateEmailTemplateRequest request) {
        EmailTemplate template = updateEmailTemplateHandler.handle(UpdateEmailTemplateCommand.builder()
                                                                           .id(NotificationIds.parseRequired(id, "id"))
                                                                           .name(request.getName())
                                                                           .subject(request.getSubject())
                                                                           .body(request.getBody())
                                                                           .attachments(mapUpdateAttachments(
                                                                                   request.getAttachments()))
                                                                           .build());
        return toResponse(template);
    }

    private EmailTemplateResponse toResponse(EmailTemplate template) {
        return emailTemplateMapper.toEmailTemplateResponse(
                template,
                emailTemplateAttachmentRepository,
                attachmentStorageService);
    }

    private List<CreateEmailTemplateCommand.Attachment> mapCreateAttachments(
            List<EmailTemplateAttachmentRequest> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<CreateEmailTemplateCommand.Attachment> mapped = new ArrayList<>();
        for (EmailTemplateAttachmentRequest attachment : attachments) {
            mapped.add(new CreateEmailTemplateCommand.Attachment(attachment.getName(), attachment.getToken()));
        }
        return mapped;
    }

    private List<UpdateEmailTemplateCommand.Attachment> mapUpdateAttachments(
            List<EmailTemplateAttachmentRequest> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<UpdateEmailTemplateCommand.Attachment> mapped = new ArrayList<>();
        for (EmailTemplateAttachmentRequest attachment : attachments) {
            mapped.add(new UpdateEmailTemplateCommand.Attachment(attachment.getName(), attachment.getToken()));
        }
        return mapped;
    }
}
