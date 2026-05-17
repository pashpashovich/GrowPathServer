package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class TraineeUserNotFoundException extends ApiException {

    public TraineeUserNotFoundException() {
        super(404, "Not Found", "User not found in trainee-service");
    }

    public TraineeUserNotFoundException(Long userId) {
        super(404, "Not Found", "User not found in trainee-service with id: " + userId);
    }
}
