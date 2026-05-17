package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class RecipientNotFoundException extends ApiException {

    public RecipientNotFoundException(Long id) {
        super(404, "Not Found", "Recipient not found with id: " + id);
    }
}
