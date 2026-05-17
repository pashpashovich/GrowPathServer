package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidRecipientDataException extends ApiException {

    public InvalidRecipientDataException(String message) {
        super(400, "Bad Request", message);
    }
}
