package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InvalidDistributionGroupDataException extends ApiException {

    public InvalidDistributionGroupDataException(String message) {
        super(400, "Bad Request", message);
    }
}
