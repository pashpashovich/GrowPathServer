package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramSelectionStageDefinitionsApi;
import by.bsuir.growpathserver.dto.model.CreateSelectionStageDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateSelectionStageDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramSelectionStageDefinitionCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
public class ProgramSelectionStageDefinitionsCatalogController extends BaseController
        implements ProgramSelectionStageDefinitionsApi {

    private final ProgramSelectionStageDefinitionCatalogService programSelectionStageDefinitionCatalogService;

    @Override
    public ResponseEntity<SelectionStageDefinitionRef> createProgramSelectionStageDefinition(
            CreateSelectionStageDefinitionRequest createSelectionStageDefinitionRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programSelectionStageDefinitionCatalogService.create(createSelectionStageDefinitionRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteProgramSelectionStageDefinition(String id) {
        return ResponseEntity.ok(programSelectionStageDefinitionCatalogService.delete(id));
    }

    @Override
    public ResponseEntity<SelectionStageDefinitionRef> getProgramSelectionStageDefinitionById(String id) {
        return ResponseEntity.ok(programSelectionStageDefinitionCatalogService.getById(id));
    }

    @Override
    public ResponseEntity<SelectionStageDefinitionCatalogListResponse> getProgramSelectionStageDefinitionsCatalog() {
        return ResponseEntity.ok(programSelectionStageDefinitionCatalogService.list());
    }

    @Override
    public ResponseEntity<SelectionStageDefinitionRef> updateProgramSelectionStageDefinition(
            String id,
            UpdateSelectionStageDefinitionRequest updateSelectionStageDefinitionRequest
    ) {
        return ResponseEntity.ok(
                programSelectionStageDefinitionCatalogService.update(id, updateSelectionStageDefinitionRequest));
    }
}
