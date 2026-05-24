package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InternProfileStatus {
    ACTIVE("active"),
    ADDITIONAL_ASSESSMENT("additional_assessment"),
    COMPLETED("completed"),
    PAUSED("paused");

    private final String apiValue;

    public static InternProfileStatus fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Intern profile status is required");
        }
        for (InternProfileStatus status : values()) {
            if (status.apiValue.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown intern profile status: " + value);
    }
}
