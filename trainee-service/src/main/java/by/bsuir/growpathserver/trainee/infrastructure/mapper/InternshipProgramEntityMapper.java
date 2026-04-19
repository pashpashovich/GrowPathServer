package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import by.bsuir.growpathserver.trainee.domain.entity.ItDirectionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.ProgramGoalDefinitionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RequirementDefinitionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.SelectionStageDefinitionEntity;
import by.bsuir.growpathserver.trainee.domain.validator.InternshipProgramValidator;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ProgramGoalDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RequirementDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.SelectionStageDefinitionRepository;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternshipProgramEntityMapper {

    @Mapping(target = "title", source = "normalizedTitle")
    @Mapping(target = "itDirection", ignore = true)
    @Mapping(target = "requirementDefinitions", ignore = true)
    @Mapping(target = "goalDefinitions", ignore = true)
    @Mapping(target = "selectionStageDefinitions", ignore = true)
    @Mapping(target = "competencies", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "internships", ignore = true)
    InternshipProgramEntity toNewEntity(
            CreateInternshipProgramCommand command,
            String normalizedTitle,
            @Context InternshipProgramCatalogRepositories catalogs
    );

    @AfterMapping
    default void afterCreateMapping(
            CreateInternshipProgramCommand command,
            @MappingTarget InternshipProgramEntity entity,
            @Context InternshipProgramCatalogRepositories catalogs
    ) {
        entity.setItDirection(resolveItDirection(command.itDirectionId(), catalogs));
        entity.getRequirementDefinitions().clear();
        entity.getRequirementDefinitions().addAll(
                resolveRequirementDefinitions(command.requirementIds(), catalogs.requirementDefinitionRepository()));
        entity.getGoalDefinitions().clear();
        entity.getGoalDefinitions().addAll(
                resolveGoalDefinitions(command.goalIds(), catalogs.programGoalDefinitionRepository()));
        entity.getSelectionStageDefinitions().clear();
        entity.getSelectionStageDefinitions().addAll(
                resolveStageDefinitions(command.selectionStageIds(), catalogs.selectionStageDefinitionRepository()));
        entity.setCompetencies(resolveCompetencyEntities(command.competencyIds(), catalogs.competencyRepository()));
    }

    default void applyUpdate(
            UpdateInternshipProgramCommand command,
            @MappingTarget InternshipProgramEntity entity,
            @Context InternshipProgramCatalogRepositories catalogs
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
        if (Objects.nonNull(command.itDirectionId())) {
            entity.setItDirection(resolveItDirection(command.itDirectionId(), catalogs));
        }
        if (Objects.nonNull(command.requirementIds())) {
            entity.getRequirementDefinitions().clear();
            entity.getRequirementDefinitions().addAll(
                    resolveRequirementDefinitions(command.requirementIds(),
                                                  catalogs.requirementDefinitionRepository()));
        }
        if (Objects.nonNull(command.goalIds())) {
            entity.getGoalDefinitions().clear();
            entity.getGoalDefinitions().addAll(
                    resolveGoalDefinitions(command.goalIds(), catalogs.programGoalDefinitionRepository()));
        }
        if (Objects.nonNull(command.selectionStageIds())) {
            entity.getSelectionStageDefinitions().clear();
            entity.getSelectionStageDefinitions().addAll(
                    resolveStageDefinitions(command.selectionStageIds(),
                                            catalogs.selectionStageDefinitionRepository()));
        }
        if (Objects.nonNull(command.competencyIds())) {
            entity.getCompetencies().clear();
            entity.getCompetencies().addAll(resolveCompetencyEntities(command.competencyIds(),
                                                                      catalogs.competencyRepository()));
        }
        if (Objects.nonNull(command.status())) {
            entity.setStatus(command.status());
        }
    }

    private static ItDirectionEntity resolveItDirection(Long itDirectionId,
                                                        InternshipProgramCatalogRepositories catalogs) {
        if (itDirectionId == null) {
            return null;
        }
        return catalogs.itDirectionRepository().findById(itDirectionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid it direction id"));
    }

    private static List<Long> dedupePreserveOrder(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        Set<Long> seen = new HashSet<>();
        List<Long> out = new ArrayList<>();
        for (Long id : ids) {
            if (id != null && seen.add(id)) {
                out.add(id);
            }
        }
        return out;
    }

    private static List<RequirementDefinitionEntity> resolveRequirementDefinitions(
            List<Long> ids,
            RequirementDefinitionRepository repo
    ) {
        List<Long> ordered = dedupePreserveOrder(ids);
        if (ordered.isEmpty()) {
            return List.of();
        }
        long found = repo.countByIdIn(ordered);
        if (found != ordered.size()) {
            throw new IllegalArgumentException("One or more requirement ids are invalid");
        }
        Map<Long, RequirementDefinitionEntity> map = repo.findAllById(ordered).stream()
                .collect(Collectors.toMap(RequirementDefinitionEntity::getId, e -> e));
        List<RequirementDefinitionEntity> out = new ArrayList<>();
        for (Long id : ordered) {
            out.add(Objects.requireNonNull(map.get(id)));
        }
        return out;
    }

    private static List<ProgramGoalDefinitionEntity> resolveGoalDefinitions(
            List<Long> ids,
            ProgramGoalDefinitionRepository repo
    ) {
        List<Long> ordered = dedupePreserveOrder(ids);
        if (ordered.isEmpty()) {
            return List.of();
        }
        long found = repo.countByIdIn(ordered);
        if (found != ordered.size()) {
            throw new IllegalArgumentException("One or more goal ids are invalid");
        }
        Map<Long, ProgramGoalDefinitionEntity> map = repo.findAllById(ordered).stream()
                .collect(Collectors.toMap(ProgramGoalDefinitionEntity::getId, e -> e));
        List<ProgramGoalDefinitionEntity> out = new ArrayList<>();
        for (Long id : ordered) {
            out.add(Objects.requireNonNull(map.get(id)));
        }
        return out;
    }

    private static List<SelectionStageDefinitionEntity> resolveStageDefinitions(
            List<Long> ids,
            SelectionStageDefinitionRepository repo
    ) {
        List<Long> ordered = dedupePreserveOrder(ids);
        if (ordered.isEmpty()) {
            return List.of();
        }
        long found = repo.countByIdIn(ordered);
        if (found != ordered.size()) {
            throw new IllegalArgumentException("One or more selection stage ids are invalid");
        }
        Map<Long, SelectionStageDefinitionEntity> map = repo.findAllById(ordered).stream()
                .collect(Collectors.toMap(SelectionStageDefinitionEntity::getId, e -> e));
        List<SelectionStageDefinitionEntity> out = new ArrayList<>();
        for (Long id : ordered) {
            out.add(Objects.requireNonNull(map.get(id)));
        }
        return out;
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
        if (Objects.nonNull(command.itDirectionId())) {
            Long currentId = entity.getItDirection() == null ? null : entity.getItDirection().getId();
            if (!Objects.equals(command.itDirectionId(), currentId)) {
                return true;
            }
        }
        if (Objects.nonNull(command.requirementIds())
                && !requirementDefinitionIdsEqual(entity, command.requirementIds())) {
            return true;
        }
        if (Objects.nonNull(command.goalIds()) && !goalDefinitionIdsEqual(entity, command.goalIds())) {
            return true;
        }
        if (Objects.nonNull(command.selectionStageIds())
                && !selectionStageDefinitionIdsEqual(entity, command.selectionStageIds())) {
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

    private static boolean requirementDefinitionIdsEqual(InternshipProgramEntity entity, List<Long> next) {
        List<Long> current = entity.getRequirementDefinitions().stream()
                .map(RequirementDefinitionEntity::getId)
                .sorted()
                .toList();
        List<Long> sortedNext = dedupePreserveOrder(next).stream().sorted().toList();
        return Objects.equals(current, sortedNext);
    }

    private static boolean goalDefinitionIdsEqual(InternshipProgramEntity entity, List<Long> next) {
        List<Long> current = entity.getGoalDefinitions().stream()
                .map(ProgramGoalDefinitionEntity::getId)
                .sorted()
                .toList();
        List<Long> sortedNext = dedupePreserveOrder(next).stream().sorted().toList();
        return Objects.equals(current, sortedNext);
    }

    private static boolean selectionStageDefinitionIdsEqual(InternshipProgramEntity entity, List<Long> next) {
        List<Long> current = entity.getSelectionStageDefinitions().stream()
                .map(SelectionStageDefinitionEntity::getId)
                .toList();
        List<Long> nextOrdered = dedupePreserveOrder(next);
        return Objects.equals(current, nextOrdered);
    }
}
