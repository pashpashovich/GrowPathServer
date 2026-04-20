package by.bsuir.growpathserver.trainee.domain.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoadmapLifecycleStatus {
    ACTIVE("active"),
    COMPLETED("completed"),
    PAUSED("paused"),
    DRAFT("draft");

    private final String value;

    public static RoadmapLifecycleStatus fromString(String value) {
        for (RoadmapLifecycleStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown roadmap status: " + value);
    }
}
