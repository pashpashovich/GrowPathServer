package by.bsuir.growpathserver.notification.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class DistributionGroupsNotFoundException extends ApiException {

    public DistributionGroupsNotFoundException() {
        super(404, "Not Found", "One or more distribution groups not found");
    }
}
