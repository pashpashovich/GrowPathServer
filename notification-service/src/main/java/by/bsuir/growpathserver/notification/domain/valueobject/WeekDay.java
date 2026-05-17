package by.bsuir.growpathserver.notification.domain.valueobject;

import java.time.DayOfWeek;

public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public static WeekDay fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Week day is required");
        }
        return switch (value.toLowerCase()) {
            case "monday" -> MONDAY;
            case "tuesday" -> TUESDAY;
            case "wednesday" -> WEDNESDAY;
            case "thursday" -> THURSDAY;
            case "friday" -> FRIDAY;
            case "saturday" -> SATURDAY;
            case "sunday" -> SUNDAY;
            default -> throw new IllegalArgumentException("Unknown week day: " + value);
        };
    }

    public String toApiValue() {
        return name().toLowerCase();
    }

    public DayOfWeek toDayOfWeek() {
        return DayOfWeek.valueOf(name());
    }
}
