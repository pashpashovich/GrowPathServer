package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ItDirectionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ProgramGoalDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RequirementDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.SelectionStageDefinitionRepository;

public record InternshipProgramCatalogRepositories(
        CompetencyRepository competencyRepository,
        RequirementDefinitionRepository requirementDefinitionRepository,
        ProgramGoalDefinitionRepository programGoalDefinitionRepository,
        SelectionStageDefinitionRepository selectionStageDefinitionRepository,
        ItDirectionRepository itDirectionRepository
) {
}
