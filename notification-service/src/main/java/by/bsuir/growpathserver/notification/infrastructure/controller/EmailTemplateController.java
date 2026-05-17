package by.bsuir.growpathserver.notification.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.EmailTemplatesApi;
import by.bsuir.growpathserver.dto.model.CreateEmailTemplateRequest;
import by.bsuir.growpathserver.dto.model.EmailTemplateListResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateResponse;
import by.bsuir.growpathserver.dto.model.PresignEmailTemplateAttachmentUploadRequest;
import by.bsuir.growpathserver.dto.model.PresignEmailTemplateAttachmentUploadResponse;
import by.bsuir.growpathserver.dto.model.UpdateEmailTemplateRequest;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'MENTOR', 'DEPARTMENT_HEAD', 'ADMIN')")
public class EmailTemplateController implements EmailTemplatesApi {

    private final EmailTemplateApplicationFacade emailTemplateApplicationFacade;

    @Override
    public ResponseEntity<EmailTemplateResponse> createEmailTemplate(CreateEmailTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailTemplateApplicationFacade.createEmailTemplate(request));
    }

    @Override
    public ResponseEntity<Void> deleteEmailTemplate(String id) {
        emailTemplateApplicationFacade.deleteEmailTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadEmailTemplateAttachment(
            String templateId,
            String attachmentId) {
        return emailTemplateApplicationFacade.downloadEmailTemplateAttachment(templateId, attachmentId);
    }

    @Override
    public ResponseEntity<EmailTemplateResponse> getEmailTemplateById(String id) {
        return ResponseEntity.ok(emailTemplateApplicationFacade.getEmailTemplateById(id));
    }

    @Override
    public ResponseEntity<EmailTemplateListResponse> getEmailTemplates(Integer page, Integer limit) {
        return ResponseEntity.ok(emailTemplateApplicationFacade.getEmailTemplates(page, limit));
    }

    @Override
    public ResponseEntity<PresignEmailTemplateAttachmentUploadResponse> presignEmailTemplateAttachmentUpload(
            PresignEmailTemplateAttachmentUploadRequest request) {
        return ResponseEntity.ok(emailTemplateApplicationFacade.presignEmailTemplateAttachmentUpload(request));
    }

    @Override
    public ResponseEntity<EmailTemplateResponse> updateEmailTemplate(String id, UpdateEmailTemplateRequest request) {
        return ResponseEntity.ok(emailTemplateApplicationFacade.updateEmailTemplate(id, request));
    }
}
