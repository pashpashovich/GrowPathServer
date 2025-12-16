package by.bsuir.growpathserver.trainee.application.query;

import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import lombok.Builder;

@Builder
public record GetInternshipProgramsQuery(
        Integer page,
        Integer limit,
        InternshipProgramStatus status,
        String search
) {
}
