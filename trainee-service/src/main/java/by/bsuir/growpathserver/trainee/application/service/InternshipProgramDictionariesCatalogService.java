package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;

public interface InternshipProgramDictionariesCatalogService {

    ItDirectionCatalogListResponse listItDirections();

    RequirementDefinitionCatalogListResponse listRequirementDefinitions();

    ProgramGoalDefinitionCatalogListResponse listProgramGoalDefinitions();

    SelectionStageDefinitionCatalogListResponse listSelectionStageDefinitions();
}
