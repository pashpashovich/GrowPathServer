package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class UnknownEnumerationValueException extends ApiException {

    public UnknownEnumerationValueException(String fieldName, String value) {
        super(400, "Bad Request", "Unknown value '%s' for %s".formatted(value, fieldName));
    }
}
