package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InternNotFoundForReportException extends ApiException {

    public InternNotFoundForReportException(Long internId) {
        super(404, "Not Found", "Intern not found: " + internId);
    }
}
