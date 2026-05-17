package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidMailingConfigurationException extends ApiException {

    public InvalidMailingConfigurationException(String message) {
        super(400, "Bad Request", message);
    }
}
