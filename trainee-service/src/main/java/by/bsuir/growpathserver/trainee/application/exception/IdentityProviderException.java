package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class IdentityProviderException extends ApiException {

    public IdentityProviderException(String message) {
        super(503, "Service Unavailable", message);
    }

    public IdentityProviderException(String message, Throwable cause) {
        super(503, "Service Unavailable", message, cause);
    }
}
