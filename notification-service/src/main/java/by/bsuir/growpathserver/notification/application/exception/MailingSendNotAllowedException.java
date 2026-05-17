package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;

public class MailingSendNotAllowedException extends ApiException {

    public MailingSendNotAllowedException(MailingStatus status) {
        super(400, "Bad Request", "Mailing cannot be sent in status " + status);
    }
}
