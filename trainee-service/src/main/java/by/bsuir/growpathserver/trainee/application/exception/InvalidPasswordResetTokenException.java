package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidPasswordResetTokenException extends ApiException {

    public InvalidPasswordResetTokenException() {
        super(422, "Unprocessable Entity",
                "Invalid or expired password reset link. Please request a new reset email.");
    }
}
