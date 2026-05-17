package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidMailingScheduleException extends ApiException {

    public InvalidMailingScheduleException(String executeTime) {
        super(400, "Bad Request", "Invalid schedule executeTime: " + executeTime);
    }
}
