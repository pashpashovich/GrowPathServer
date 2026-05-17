package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class DistributionGroupNotFoundException extends ApiException {

    public DistributionGroupNotFoundException(Long id) {
        super(404, "Not Found", "Distribution group not found with id: " + id);
    }
}
