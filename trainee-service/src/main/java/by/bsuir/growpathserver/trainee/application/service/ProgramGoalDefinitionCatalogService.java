package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.CreateProgramGoalDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateProgramGoalDefinitionRequest;

public interface ProgramGoalDefinitionCatalogService {

    ProgramGoalDefinitionCatalogListResponse list();

    ProgramGoalDefinitionRef getById(String id);

    ProgramGoalDefinitionRef create(CreateProgramGoalDefinitionRequest request);

    ProgramGoalDefinitionRef update(String id, UpdateProgramGoalDefinitionRequest request);

    MessageResponse delete(String id);
}
