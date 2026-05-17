package by.bsuir.growpathserver.notification.domain.valueobject;

public enum MailingType {
    IMMEDIATE,
    SCHEDULED,
    RECURRING;

    public static MailingType fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Mailing type is required");
        }
        return switch (value.toLowerCase()) {
            case "immediate" -> IMMEDIATE;
            case "scheduled" -> SCHEDULED;
            case "recurring" -> RECURRING;
            default -> throw new IllegalArgumentException("Unknown mailing type: " + value);
        };
    }

    public String toApiValue() {
        return name().toLowerCase();
    }
}
