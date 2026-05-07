package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramGoalDefinitionsApi;
import by.bsuir.growpathserver.dto.model.CreateProgramGoalDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateProgramGoalDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramGoalDefinitionCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
public class ProgramGoalDefinitionsCatalogController extends BaseController implements ProgramGoalDefinitionsApi {

    private final ProgramGoalDefinitionCatalogService programGoalDefinitionCatalogService;

    @Override
    public ResponseEntity<ProgramGoalDefinitionRef> createProgramGoalDefinition(
            CreateProgramGoalDefinitionRequest createProgramGoalDefinitionRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programGoalDefinitionCatalogService.create(createProgramGoalDefinitionRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteProgramGoalDefinition(String id) {
        return ResponseEntity.ok(programGoalDefinitionCatalogService.delete(id));
    }

    @Override
    public ResponseEntity<ProgramGoalDefinitionRef> getProgramGoalDefinitionById(String id) {
        return ResponseEntity.ok(programGoalDefinitionCatalogService.getById(id));
    }

    @Override
    public ResponseEntity<ProgramGoalDefinitionCatalogListResponse> getProgramGoalDefinitionsCatalog() {
        return ResponseEntity.ok(programGoalDefinitionCatalogService.list());
    }

    @Override
    public ResponseEntity<ProgramGoalDefinitionRef> updateProgramGoalDefinition(
            String id,
            UpdateProgramGoalDefinitionRequest updateProgramGoalDefinitionRequest
    ) {
        return ResponseEntity.ok(programGoalDefinitionCatalogService.update(id, updateProgramGoalDefinitionRequest));
    }
}
