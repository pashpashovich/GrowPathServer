package by.bsuir.growpathserver.notification.domain.valueobject;

public enum RecipientType {
    USER,
    EXTERNAL;

    public static RecipientType fromApiValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Recipient type is required");
        }
        return switch (value.toLowerCase()) {
            case "user" -> USER;
            case "external" -> EXTERNAL;
            default -> throw new IllegalArgumentException("Unknown recipient type: " + value);
        };
    }

    public String toApiValue() {
        return name().toLowerCase();
    }
}
