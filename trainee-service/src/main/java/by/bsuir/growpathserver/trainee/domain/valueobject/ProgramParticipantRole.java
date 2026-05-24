package by.bsuir.growpathserver.trainee.domain.valueobject;

public enum ProgramParticipantRole {
    MENTOR,
    INTERN;

    public static ProgramParticipantRole fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Participant role is required");
        }
        return switch (value.toUpperCase()) {
            case "MENTOR" -> MENTOR;
            case "INTERN" -> INTERN;
            default -> throw new IllegalArgumentException("Unknown participant role: " + value);
        };
    }
}
