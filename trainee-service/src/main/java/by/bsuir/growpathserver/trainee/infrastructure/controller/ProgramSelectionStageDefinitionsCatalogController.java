package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramSelectionStageDefinitionsApi;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramDictionariesCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class ProgramSelectionStageDefinitionsCatalogController extends BaseController
        implements ProgramSelectionStageDefinitionsApi {

    private final InternshipProgramDictionariesCatalogService internshipProgramDictionariesCatalogService;

    @Override
    public ResponseEntity<SelectionStageDefinitionCatalogListResponse> getProgramSelectionStageDefinitionsCatalog() {
        return ResponseEntity.ok(internshipProgramDictionariesCatalogService.listSelectionStageDefinitions());
    }
}
