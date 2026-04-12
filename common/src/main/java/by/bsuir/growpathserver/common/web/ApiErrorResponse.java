package by.bsuir.growpathserver.common.web;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;

@Setter
public class ApiErrorResponse {
    private String error;
    private String message;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

}
