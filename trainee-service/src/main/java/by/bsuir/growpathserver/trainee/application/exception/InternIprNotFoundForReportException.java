package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InternIprNotFoundForReportException extends ApiException {

    public InternIprNotFoundForReportException(Long internId) {
        super(404, "Not Found", "No individual development plan found for intern: " + internId);
    }
}
