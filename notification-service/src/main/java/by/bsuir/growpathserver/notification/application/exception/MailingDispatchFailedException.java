package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class MailingDispatchFailedException extends ApiException {

    public MailingDispatchFailedException(Long mailingId) {
        super(400, "Bad Request", "No emails were sent for mailing " + mailingId);
    }

    public MailingDispatchFailedException(Long mailingId, String reason) {
        super(400, "Bad Request", "Failed to dispatch mailing " + mailingId + ": " + reason);
    }
}
