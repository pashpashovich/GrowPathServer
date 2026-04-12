package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramGoalEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramRequirementEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramSelectionStageEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import lombok.Getter;

@Getter
public class InternshipProgram {
    private final Long id;
    private final String title;
    private final String description;
    private final LocalDate startDate;
    private final Integer duration;
    private final Integer maxPlaces;
    private final List<String> requirements;
    private final List<InternshipProgramGoalItem> goals;
    private final String itDirection;
    private final List<ProgramCompetency> competencyRefs;
    private final List<InternshipProgramStageItem> selectionStages;
    private final InternshipProgramStatus status;
    private final Long createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String internships;

    private InternshipProgram(InternshipProgramEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startDate = entity.getStartDate();
        this.duration = entity.getDuration();
        this.maxPlaces = entity.getMaxPlaces();
        this.requirements = mapRequirements(entity);
        this.goals = mapGoals(entity);
        this.itDirection = entity.getItDirection();
        this.competencyRefs = mapCompetencies(entity.getCompetencies());
        this.selectionStages = mapSelectionStages(entity);
        this.status = entity.getStatus();
        this.createdBy = entity.getCreatedBy();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.internships = entity.getInternships();
    }

    private static List<String> mapRequirements(InternshipProgramEntity entity) {
        return entity.getRequirementItems().stream()
                .sorted(Comparator.comparingLong(InternshipProgramRequirementEntity::getId))
                .map(InternshipProgramRequirementEntity::getRequirementText)
                .toList();
    }

    private static List<InternshipProgramGoalItem> mapGoals(InternshipProgramEntity entity) {
        return entity.getGoalItems().stream()
                .sorted(Comparator.comparingLong(InternshipProgramGoalEntity::getId))
                .map(g -> new InternshipProgramGoalItem(g.getId(), g.getTitle(), g.getDescription()))
                .toList();
    }

    private static List<InternshipProgramStageItem> mapSelectionStages(InternshipProgramEntity entity) {
        List<InternshipProgramSelectionStageEntity> ordered = entity.getSelectionStageItems().stream()
                .sorted(Comparator.comparingLong(InternshipProgramSelectionStageEntity::getId))
                .toList();
        List<InternshipProgramStageItem> out = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            InternshipProgramSelectionStageEntity s = ordered.get(i);
            out.add(new InternshipProgramStageItem(
                    s.getId(),
                    s.getName(),
                    s.getDescription(),
                    i + 1,
                    s.isActive()));
        }
        return List.copyOf(out);
    }

    private static List<ProgramCompetency> mapCompetencies(Set<CompetencyEntity> set) {
        return set.stream()
                .map(c -> new ProgramCompetency(c.getId(), c.getName()))
                .sorted(Comparator.comparing(ProgramCompetency::id))
                .toList();
    }

    public static InternshipProgram fromEntity(InternshipProgramEntity entity) {
        return new InternshipProgram(entity);
    }
}
