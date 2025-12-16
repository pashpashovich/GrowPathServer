package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
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
    private final String requirements;
    private final String goals;
    private final String competencies;
    private final String selectionStages;
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
        this.requirements = entity.getRequirements();
        this.goals = entity.getGoals();
        this.competencies = entity.getCompetencies();
        this.selectionStages = entity.getSelectionStages();
        this.status = entity.getStatus();
        this.createdBy = entity.getCreatedBy();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.internships = entity.getInternships();
    }

    public static InternshipProgram fromEntity(InternshipProgramEntity entity) {
        return new InternshipProgram(entity);
    }

    public InternshipProgramEntity toEntity() {
        InternshipProgramEntity entity = new InternshipProgramEntity();
        entity.setId(this.id);
        entity.setTitle(this.title);
        entity.setDescription(this.description);
        entity.setStartDate(this.startDate);
        entity.setDuration(this.duration);
        entity.setMaxPlaces(this.maxPlaces);
        entity.setRequirements(this.requirements);
        entity.setGoals(this.goals);
        entity.setCompetencies(this.competencies);
        entity.setSelectionStages(this.selectionStages);
        entity.setStatus(this.status);
        entity.setCreatedBy(this.createdBy);
        entity.setCreatedAt(this.createdAt);
        entity.setUpdatedAt(this.updatedAt);
        entity.setInternships(this.internships);
        return entity;
    }

    public static InternshipProgram create(String title, String description, LocalDate startDate,
                                           Integer duration, Integer maxPlaces, String requirements,
                                           String goals, String competencies, String selectionStages,
                                           InternshipProgramStatus status, Long createdBy) {
        InternshipProgramEntity entity = new InternshipProgramEntity();
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setStartDate(startDate);
        entity.setDuration(duration);
        entity.setMaxPlaces(maxPlaces);
        entity.setRequirements(requirements);
        entity.setGoals(goals);
        entity.setCompetencies(competencies);
        entity.setSelectionStages(selectionStages);
        entity.setStatus(status != null ? status : InternshipProgramStatus.DRAFT);
        entity.setCreatedBy(createdBy);
        return new InternshipProgram(entity);
    }
}
