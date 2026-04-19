package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramGoalDefinitionsApi;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramDictionariesCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class ProgramGoalDefinitionsCatalogController extends BaseController implements ProgramGoalDefinitionsApi {

    private final InternshipProgramDictionariesCatalogService internshipProgramDictionariesCatalogService;

    @Override
    public ResponseEntity<ProgramGoalDefinitionCatalogListResponse> getProgramGoalDefinitionsCatalog() {
        return ResponseEntity.ok(internshipProgramDictionariesCatalogService.listProgramGoalDefinitions());
    }
}
