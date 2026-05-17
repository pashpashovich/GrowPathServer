package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class MailingAlreadySentException extends ApiException {

    public MailingAlreadySentException() {
        super(400, "Bad Request", "Cannot update sent mailing");
    }
}
