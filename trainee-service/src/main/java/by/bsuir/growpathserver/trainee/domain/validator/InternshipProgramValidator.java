package by.bsuir.growpathserver.trainee.domain.validator;

import java.time.LocalDate;
import java.util.Objects;

import by.bsuir.growpathserver.trainee.domain.exception.InternshipProgramLockedException;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternshipProgramValidator {

    public static final int MIN_DURATION_MONTHS = 1;
    public static final int MAX_DURATION_MONTHS = 12;

    public static void validateDurationMonths(Integer duration) {
        if (Objects.isNull(duration)) {
            throw new IllegalArgumentException("Internship duration is required");
        }
        if (duration < MIN_DURATION_MONTHS || duration > MAX_DURATION_MONTHS) {
            throw new IllegalArgumentException(
                    "Internship duration must be between " + MIN_DURATION_MONTHS + " and " + MAX_DURATION_MONTHS
                            + " months");
        }
    }

    public static void assertUpdateAllowedAfterStart(
            LocalDate startDate,
            InternshipProgramStatus currentStatus,
            LocalDate today,
            InternshipProgramStatus requestedStatus,
            boolean structuralFieldsChanged
    ) {
        if (Objects.isNull(startDate) || today.isBefore(startDate)) {
            return;
        }
        if (structuralFieldsChanged) {
            throw new InternshipProgramLockedException();
        }
        if (InternshipProgramStatus.ARCHIVED.equals(currentStatus)) {
            if (Objects.nonNull(requestedStatus) && !InternshipProgramStatus.ARCHIVED.equals(requestedStatus)) {
                throw new InternshipProgramLockedException();
            }
            return;
        }
        if (Objects.nonNull(requestedStatus) && !currentStatus.equals(requestedStatus)
                && !InternshipProgramStatus.ARCHIVED.equals(requestedStatus)) {
            throw new InternshipProgramLockedException();
        }
    }
}
