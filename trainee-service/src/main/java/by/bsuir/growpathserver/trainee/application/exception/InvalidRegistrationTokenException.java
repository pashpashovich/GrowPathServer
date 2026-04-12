package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidRegistrationTokenException extends ApiException {

    public InvalidRegistrationTokenException() {
        super(422, "Unprocessable Entity",
                "Invalid or expired registration link. Please request a new invitation.");
    }
}
