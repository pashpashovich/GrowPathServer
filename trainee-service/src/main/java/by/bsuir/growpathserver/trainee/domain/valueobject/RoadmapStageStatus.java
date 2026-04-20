package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoadmapStageStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    DELAYED("delayed");

    private final String value;

    public static RoadmapStageStatus fromString(String value) {
        for (RoadmapStageStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown stage status: " + value);
    }
}
