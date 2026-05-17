package by.bsuir.growpathserver.trainee.application.exception;

import by.bsuir.growpathserver.common.web.ApiException;

public class InternshipProgramNotFoundForReportException extends ApiException {

    public InternshipProgramNotFoundForReportException(Long programId) {
        super(404, "Not Found", "Internship program not found: " + programId);
    }
}
