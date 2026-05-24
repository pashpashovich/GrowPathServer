package by.bsuir.growpathserver.trainee.domain.valueobject;

public enum HiringDecisionType {
    RECOMMENDED_FOR_HIRE("recommended_for_hire"),
    TALENT_RESERVE("talent_reserve"),
    COMPLETED_WITHOUT_HIRE("completed_without_hire"),
    ADDITIONAL_ASSESSMENT("additional_assessment");

    private final String apiValue;

    HiringDecisionType(String apiValue) {
        this.apiValue = apiValue;
    }

    public static HiringDecisionType fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Hiring decision is required");
        }
        String normalized = value.trim().toLowerCase();
        for (HiringDecisionType type : values()) {
            if (type.apiValue.equals(normalized)) {
                return type;
            }
        }
        return switch (normalized) {
            case "hire" -> RECOMMENDED_FOR_HIRE;
            case "reject" -> COMPLETED_WITHOUT_HIRE;
            default -> throw new IllegalArgumentException("Unknown hiring decision: " + value);
        };
    }

    public String toApiValue() {
        return apiValue;
    }

    public boolean isFinalDecision() {
        return this != ADDITIONAL_ASSESSMENT;
    }

    public InternProfileStatus toInternProfileStatus() {
        return switch (this) {
            case ADDITIONAL_ASSESSMENT -> InternProfileStatus.ADDITIONAL_ASSESSMENT;
            case RECOMMENDED_FOR_HIRE, TALENT_RESERVE, COMPLETED_WITHOUT_HIRE -> InternProfileStatus.COMPLETED;
        };
    }

    public String toDisplayLabel() {
        return switch (this) {
            case RECOMMENDED_FOR_HIRE -> "Рекомендован к найму";
            case TALENT_RESERVE -> "Зачисление в кадровый резерв";
            case COMPLETED_WITHOUT_HIRE -> "Завершение стажировки без найма";
            case ADDITIONAL_ASSESSMENT -> "Дополнительная оценка";
        };
    }
}
