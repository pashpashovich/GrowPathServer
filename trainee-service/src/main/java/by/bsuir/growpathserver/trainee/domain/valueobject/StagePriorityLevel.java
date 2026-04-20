package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StagePriorityLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    public static StagePriorityLevel fromString(String value) {
        for (StagePriorityLevel p : values()) {
            if (p.value.equalsIgnoreCase(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown stage priority: " + value);
    }
}
