package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramRequirementDefinitionsApi;
import by.bsuir.growpathserver.dto.model.CreateRequirementDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateRequirementDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramRequirementDefinitionCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
public class ProgramRequirementDefinitionsCatalogController extends BaseController
        implements ProgramRequirementDefinitionsApi {

    private final ProgramRequirementDefinitionCatalogService programRequirementDefinitionCatalogService;

    @Override
    public ResponseEntity<RequirementDefinitionRef> createProgramRequirementDefinition(
            CreateRequirementDefinitionRequest createRequirementDefinitionRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programRequirementDefinitionCatalogService.create(createRequirementDefinitionRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteProgramRequirementDefinition(String id) {
        return ResponseEntity.ok(programRequirementDefinitionCatalogService.delete(id));
    }

    @Override
    public ResponseEntity<RequirementDefinitionRef> getProgramRequirementDefinitionById(String id) {
        return ResponseEntity.ok(programRequirementDefinitionCatalogService.getById(id));
    }

    @Override
    public ResponseEntity<RequirementDefinitionCatalogListResponse> getProgramRequirementDefinitionsCatalog() {
        return ResponseEntity.ok(programRequirementDefinitionCatalogService.list());
    }

    @Override
    public ResponseEntity<RequirementDefinitionRef> updateProgramRequirementDefinition(
            String id,
            UpdateRequirementDefinitionRequest updateRequirementDefinitionRequest
    ) {
        return ResponseEntity.ok(
                programRequirementDefinitionCatalogService.update(id, updateRequirementDefinitionRequest));
    }
}
