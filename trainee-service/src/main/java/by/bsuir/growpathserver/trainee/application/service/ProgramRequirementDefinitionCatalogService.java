package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.CreateRequirementDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateRequirementDefinitionRequest;

public interface ProgramRequirementDefinitionCatalogService {

    RequirementDefinitionCatalogListResponse list();

    RequirementDefinitionRef getById(String id);

    RequirementDefinitionRef create(CreateRequirementDefinitionRequest request);

    RequirementDefinitionRef update(String id, UpdateRequirementDefinitionRequest request);

    MessageResponse delete(String id);
}
