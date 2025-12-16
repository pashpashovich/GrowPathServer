package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InternshipProgramStatus {
    ACTIVE("active"),
    DRAFT("draft"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    public static InternshipProgramStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        for (InternshipProgramStatus status : InternshipProgramStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown internship program status: " + value);
    }
}
