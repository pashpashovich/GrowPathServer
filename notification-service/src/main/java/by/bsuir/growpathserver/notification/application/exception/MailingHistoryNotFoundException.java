package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class MailingHistoryNotFoundException extends ApiException {

    public MailingHistoryNotFoundException(Long id) {
        super(404, "Not Found", "Mailing history record not found with id: " + id);
    }
}
