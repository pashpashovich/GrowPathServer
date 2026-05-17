package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class EmailDeliveryException extends ApiException {

    public EmailDeliveryException(String recipientEmail, Throwable cause) {
        super(502, "Bad Gateway", "Failed to send email to " + recipientEmail, cause);
    }
}
