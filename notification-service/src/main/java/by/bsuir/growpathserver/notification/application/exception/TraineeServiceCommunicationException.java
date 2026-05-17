package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class TraineeServiceCommunicationException extends ApiException {

    public TraineeServiceCommunicationException(int status) {
        super(502, "Bad Gateway", "Failed to communicate with trainee-service (HTTP " + status + ")");
    }
}
