package by.bsuir.growpathserver.trainee.application.query;

import java.time.LocalDate;

import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import lombok.Builder;

@Builder
public record GetInternshipProgramsQuery(
        Integer page,
        Integer limit,
        InternshipProgramStatus status,
        String search,
        String itDirection,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        Integer maxPlacesMin,
        Integer maxPlacesMax,
        Long competencyId,
        Boolean includeArchived
) {
}
