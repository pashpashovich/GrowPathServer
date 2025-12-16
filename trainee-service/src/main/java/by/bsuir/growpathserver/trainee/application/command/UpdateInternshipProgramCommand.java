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
        List<String> requirements,
        List<ProgramGoal> goals,
        List<String> competencies,
        List<SelectionStage> selectionStages,
        InternshipProgramStatus status
) {
    public record ProgramGoal(String title, String description) {
    }

    public record SelectionStage(String name, String description, Integer order) {
    }
}
