package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InternalRecipientUserIdRequiredException extends ApiException {

    public InternalRecipientUserIdRequiredException() {
        super(400, "Bad Request", "userId is required when recipient type is user");
    }
}
