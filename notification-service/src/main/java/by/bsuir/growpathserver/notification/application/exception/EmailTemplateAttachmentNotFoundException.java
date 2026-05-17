package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class EmailTemplateAttachmentNotFoundException extends ApiException {

    public EmailTemplateAttachmentNotFoundException(Long templateId, Long attachmentId) {
        super(404, "Not Found",
              "Attachment not found with id: " + attachmentId + " for template: " + templateId);
    }
}
