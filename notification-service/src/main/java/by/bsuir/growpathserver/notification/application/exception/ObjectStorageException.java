package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class ObjectStorageException extends ApiException {

    public ObjectStorageException(String message, Throwable cause) {
        super(500, "Internal Server Error", message, cause);
    }
}
