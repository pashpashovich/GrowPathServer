package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InternshipResultReportAccessDeniedException extends ApiException {

    public InternshipResultReportAccessDeniedException() {
        super(403, "Forbidden", "You are not allowed to view this intern's report");
    }
}
