package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.dto.model.CreateSelectionStageDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateSelectionStageDefinitionRequest;

public interface ProgramSelectionStageDefinitionCatalogService {

    SelectionStageDefinitionCatalogListResponse list();

    SelectionStageDefinitionRef getById(String id);

    SelectionStageDefinitionRef create(CreateSelectionStageDefinitionRequest request);

    SelectionStageDefinitionRef update(String id, UpdateSelectionStageDefinitionRequest request);

    MessageResponse delete(String id);
}
