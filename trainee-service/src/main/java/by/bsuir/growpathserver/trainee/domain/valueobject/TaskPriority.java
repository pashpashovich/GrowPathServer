package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    public static TaskPriority fromString(String value) {
        if (value == null) {
            return null;
        }
        for (TaskPriority priority : TaskPriority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown task priority: " + value);
    }
}
