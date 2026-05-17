package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidNotificationIdException extends ApiException {

    public InvalidNotificationIdException(String fieldName) {
        super(400, "Bad Request", "Invalid ID format for '%s'".formatted(fieldName));
    }
}
