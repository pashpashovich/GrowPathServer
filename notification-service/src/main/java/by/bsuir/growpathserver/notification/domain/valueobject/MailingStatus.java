package by.bsuir.growpathserver.notification.domain.valueobject;

public enum MailingStatus {
    DRAFT,
    SCHEDULED,
    SENT,
    CANCELLED;

    public static MailingStatus fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Mailing status is required");
        }
        return switch (value.toLowerCase()) {
            case "draft" -> DRAFT;
            case "scheduled" -> SCHEDULED;
            case "sent" -> SENT;
            case "cancelled" -> CANCELLED;
            default -> throw new IllegalArgumentException("Unknown mailing status: " + value);
        };
    }

    public String toApiValue() {
        return name().toLowerCase();
    }
}
