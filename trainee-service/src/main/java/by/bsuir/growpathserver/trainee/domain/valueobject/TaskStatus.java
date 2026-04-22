package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    SUBMITTED("submitted"),
    ON_REVIEW("on_review"),
    NEEDS_REWORK("needs_rework"),
    COMPLETED("completed"),
    REJECTED("rejected");

    private final String value;

    public static TaskStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status: " + value);
    }
}
