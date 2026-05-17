package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class ExternalRecipientUserIdNotAllowedException extends ApiException {

    public ExternalRecipientUserIdNotAllowedException() {
        super(400, "Bad Request", "userId must not be provided when recipient type is external");
    }
}
