package by.bsuir.growpathserver.trainee.domain.valueobject;

public enum HiringDecisionType {
    HIRE,
    REJECT;

    public static HiringDecisionType fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Hiring decision is required");
        }
        return switch (value.trim().toLowerCase()) {
            case "hire" -> HIRE;
            case "reject" -> REJECT;
            default -> throw new IllegalArgumentException("Unknown hiring decision: " + value);
        };
    }

    public String toApiValue() {
        return name().toLowerCase();
    }
}
