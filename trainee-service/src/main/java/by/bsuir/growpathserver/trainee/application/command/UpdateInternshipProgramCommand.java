package by.bsuir.growpathserver.trainee.application.command;

import java.time.LocalDate;
import java.util.List;

import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import lombok.Builder;

@Builder
public record UpdateInternshipProgramCommand(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        Integer duration,
        Integer maxPlaces,
        Long itDirectionId,
        List<Long> competencyIds,
        List<Long> requirementIds,
        List<Long> goalIds,
        List<Long> selectionStageIds,
        InternshipProgramStatus status
) {
}
