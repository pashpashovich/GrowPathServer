package by.bsuir.growpathserver.common.web;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int httpStatus;
    private final String error;

    public ApiException(int httpStatus, String error, String message) {
        super(message != null ? message : "");
        this.httpStatus = httpStatus;
        this.error = error;
    }

    public ApiException(int httpStatus, String error, String message, Throwable cause) {
        super(message != null ? message : "", cause);
        this.httpStatus = httpStatus;
        this.error = error;
    }
}
