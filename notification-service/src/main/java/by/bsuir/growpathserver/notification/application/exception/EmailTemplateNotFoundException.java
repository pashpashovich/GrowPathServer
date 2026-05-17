package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class EmailTemplateNotFoundException extends ApiException {

    public EmailTemplateNotFoundException(Long id) {
        super(404, "Not Found", "Email template not found with id: " + id);
    }
}
