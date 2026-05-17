package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class DuplicateRecipientEmailException extends ApiException {

    public DuplicateRecipientEmailException(String email) {
        super(409, "Conflict", "Recipient with email '%s' already exists".formatted(email));
    }
}
