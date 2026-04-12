package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramGoalEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramRequirementEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramSelectionStageEntity;
import by.bsuir.growpathserver.trainee.domain.validator.InternshipProgramValidator;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternshipProgramEntityMapper {

    @Mapping(target = "title", source = "normalizedTitle")
    @Mapping(target = "itDirection", ignore = true)
    @Mapping(target = "requirementItems", ignore = true)
    @Mapping(target = "goalItems", ignore = true)
    @Mapping(target = "selectionStageItems", ignore = true)
    @Mapping(target = "competencies", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "internships", ignore = true)
    InternshipProgramEntity toNewEntity(
            CreateInternshipProgramCommand command,
            String normalizedTitle,
            @Context CompetencyRepository competencyRepository
    );

    @AfterMapping
    default void afterCreateMapping(
            CreateInternshipProgramCommand command,
            @MappingTarget InternshipProgramEntity entity,
            @Context CompetencyRepository competencyRepository
    ) {
        entity.setItDirection(normalizeItDirection(command.itDirection()));
        replaceRequirementItems(entity, command.requirements());
        replaceGoalItemsFromCreate(entity, command.goals());
        replaceSelectionStagesFromCreate(entity, command.selectionStages());
        entity.setCompetencies(resolveCompetencyEntities(command.competencyIds(), competencyRepository));
    }

    default void applyUpdate(
            UpdateInternshipProgramCommand command,
            @MappingTarget InternshipProgramEntity entity,
            @Context CompetencyRepository competencyRepository
    ) {
        if (Objects.nonNull(command.title())) {
            entity.setTitle(command.title().trim());
        }
        if (Objects.nonNull(command.description())) {
            entity.setDescription(command.description());
        }
        if (Objects.nonNull(command.startDate())) {
            entity.setStartDate(command.startDate());
        }
        if (Objects.nonNull(command.duration())) {
            InternshipProgramValidator.validateDurationMonths(command.duration());
            entity.setDuration(command.duration());
        }
        if (Objects.nonNull(command.maxPlaces())) {
            entity.setMaxPlaces(command.maxPlaces());
        }
        if (Objects.nonNull(command.itDirection())) {
            entity.setItDirection(normalizeItDirection(command.itDirection()));
        }
        if (Objects.nonNull(command.requirements())) {
            replaceRequirementItems(entity, command.requirements());
        }
        if (Objects.nonNull(command.goals())) {
            replaceGoalItemsFromUpdate(entity, command.goals());
        }
        if (Objects.nonNull(command.selectionStages())) {
            replaceSelectionStagesFromUpdate(entity, command.selectionStages());
        }
        if (Objects.nonNull(command.competencyIds())) {
            entity.getCompetencies().clear();
            entity.getCompetencies().addAll(resolveCompetencyEntities(command.competencyIds(), competencyRepository));
        }
        if (Objects.nonNull(command.status())) {
            entity.setStatus(command.status());
        }
    }

    private static void replaceRequirementItems(InternshipProgramEntity entity, List<String> requirements) {
        entity.getRequirementItems().clear();
        if (CollectionUtils.isEmpty(requirements)) {
            return;
        }
        for (String text : requirements) {
            if (StringUtils.isBlank(text)) {
                continue;
            }
            InternshipProgramRequirementEntity row = new InternshipProgramRequirementEntity();
            row.setInternshipProgram(entity);
            row.setRequirementText(text.trim());
            entity.getRequirementItems().add(row);
        }
    }

    private static void replaceGoalItemsFromCreate(InternshipProgramEntity entity,
                                                   List<CreateInternshipProgramCommand.ProgramGoal> goals) {
        entity.getGoalItems().clear();
        if (CollectionUtils.isEmpty(goals)) {
            return;
        }
        for (CreateInternshipProgramCommand.ProgramGoal g : goals) {
            InternshipProgramGoalEntity row = new InternshipProgramGoalEntity();
            row.setInternshipProgram(entity);
            row.setTitle(Objects.requireNonNullElse(g.title(), ""));
            row.setDescription(g.description());
            entity.getGoalItems().add(row);
        }
    }

    private static void replaceGoalItemsFromUpdate(InternshipProgramEntity entity,
                                                   List<UpdateInternshipProgramCommand.ProgramGoal> goals) {
        entity.getGoalItems().clear();
        if (CollectionUtils.isEmpty(goals)) {
            return;
        }
        for (UpdateInternshipProgramCommand.ProgramGoal g : goals) {
            InternshipProgramGoalEntity row = new InternshipProgramGoalEntity();
            row.setInternshipProgram(entity);
            row.setTitle(Objects.requireNonNullElse(g.title(), ""));
            row.setDescription(g.description());
            entity.getGoalItems().add(row);
        }
    }

    private static void replaceSelectionStagesFromCreate(InternshipProgramEntity entity,
                                                         List<CreateInternshipProgramCommand.SelectionStage> stages) {
        entity.getSelectionStageItems().clear();
        if (CollectionUtils.isEmpty(stages)) {
            return;
        }
        for (CreateInternshipProgramCommand.SelectionStage s : stages) {
            InternshipProgramSelectionStageEntity row = new InternshipProgramSelectionStageEntity();
            row.setInternshipProgram(entity);
            row.setName(Objects.requireNonNullElse(s.name(), ""));
            row.setDescription(s.description());
            row.setActive(true);
            entity.getSelectionStageItems().add(row);
        }
    }

    private static void replaceSelectionStagesFromUpdate(InternshipProgramEntity entity,
                                                         List<UpdateInternshipProgramCommand.SelectionStage> stages) {
        entity.getSelectionStageItems().clear();
        if (CollectionUtils.isEmpty(stages)) {
            return;
        }
        for (UpdateInternshipProgramCommand.SelectionStage s : stages) {
            InternshipProgramSelectionStageEntity row = new InternshipProgramSelectionStageEntity();
            row.setInternshipProgram(entity);
            row.setName(Objects.requireNonNullElse(s.name(), ""));
            row.setDescription(s.description());
            row.setActive(true);
            entity.getSelectionStageItems().add(row);
        }
    }

    private static Set<CompetencyEntity> resolveCompetencyEntities(List<Long> competencyIds,
                                                                   CompetencyRepository repo) {
        if (CollectionUtils.isEmpty(competencyIds)) {
            return new HashSet<>();
        }
        long found = repo.countByIdIn(competencyIds);
        if (found != competencyIds.size()) {
            throw new IllegalArgumentException("One or more competency ids are invalid");
        }
        return new HashSet<>(repo.findAllById(competencyIds));
    }

    private static String normalizeItDirection(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    default boolean isStructuralChange(UpdateInternshipProgramCommand command, InternshipProgramEntity entity) {
        if (Objects.nonNull(command.title()) && !Objects.equals(command.title().trim(), entity.getTitle())) {
            return true;
        }
        if (Objects.nonNull(command.description())
                && !Objects.equals(command.description(), entity.getDescription())) {
            return true;
        }
        if (Objects.nonNull(command.startDate()) && !Objects.equals(command.startDate(), entity.getStartDate())) {
            return true;
        }
        if (Objects.nonNull(command.duration()) && !Objects.equals(command.duration(), entity.getDuration())) {
            return true;
        }
        if (Objects.nonNull(command.maxPlaces()) && !Objects.equals(command.maxPlaces(), entity.getMaxPlaces())) {
            return true;
        }
        if (Objects.nonNull(command.itDirection())) {
            if (!Objects.equals(normalizeItDirection(command.itDirection()),
                                normalizeItDirection(entity.getItDirection()))) {
                return true;
            }
        }
        if (Objects.nonNull(command.requirements())
                && !requirementsMultisetEqual(entity, command.requirements())) {
            return true;
        }
        if (Objects.nonNull(command.goals()) && !goalsEqualUpdate(entity, command.goals())) {
            return true;
        }
        if (Objects.nonNull(command.selectionStages())
                && !selectionStagesEqualUpdate(entity, command.selectionStages())) {
            return true;
        }
        if (Objects.nonNull(command.competencyIds())) {
            Set<Long> currentIds = entity.getCompetencies().stream()
                    .map(CompetencyEntity::getId)
                    .collect(Collectors.toSet());
            Set<Long> nextIds = new HashSet<>(command.competencyIds());
            if (!currentIds.equals(nextIds)) {
                return true;
            }
        }
        return false;
    }

    private static boolean requirementsMultisetEqual(InternshipProgramEntity entity, List<String> next) {
        List<String> current = entity.getRequirementItems().stream()
                .map(InternshipProgramRequirementEntity::getRequirementText)
                .sorted()
                .toList();
        List<String> sortedNext = next.stream()
                .filter(s -> !StringUtils.isBlank(s))
                .map(String::trim)
                .sorted()
                .toList();
        return Objects.equals(current, sortedNext);
    }

    private static boolean goalsEqualUpdate(InternshipProgramEntity entity,
                                            List<UpdateInternshipProgramCommand.ProgramGoal> next) {
        List<String> current = entity.getGoalItems().stream()
                .map(g -> goalKey(g.getTitle(), g.getDescription()))
                .sorted()
                .toList();
        List<String> nextKeys = next.stream()
                .map(g -> goalKey(g.title(), g.description()))
                .sorted()
                .toList();
        return Objects.equals(current, nextKeys);
    }

    private static boolean selectionStagesEqualUpdate(InternshipProgramEntity entity,
                                                      List<UpdateInternshipProgramCommand.SelectionStage> next) {
        List<String> current = entity.getSelectionStageItems().stream()
                .map(s -> stageKey(s.getName(), s.getDescription()))
                .sorted()
                .toList();
        List<String> nextKeys = next.stream()
                .map(s -> stageKey(s.name(), s.description()))
                .sorted()
                .toList();
        return Objects.equals(current, nextKeys);
    }

    private static String goalKey(String title, String description) {
        return Objects.requireNonNullElse(title, "") + "\0" + Objects.toString(description, "");
    }

    private static String stageKey(String name, String description) {
        return Objects.requireNonNullElse(name, "") + "\0" + Objects.toString(description, "");
    }
}