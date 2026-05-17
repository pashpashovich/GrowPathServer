package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class ExternalRecipientDataRequiredException extends ApiException {

    public ExternalRecipientDataRequiredException() {
        super(400, "Bad Request", "email and fullName are required when recipient type is external");
    }
}
