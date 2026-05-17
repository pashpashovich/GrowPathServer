package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class MailingNotFoundException extends ApiException {

    public MailingNotFoundException(Long id) {
        super(404, "Not Found", "Mailing not found with id: " + id);
    }
}
