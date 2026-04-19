package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ProgramRequirementDefinitionsApi;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramDictionariesCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class ProgramRequirementDefinitionsCatalogController extends BaseController
        implements ProgramRequirementDefinitionsApi {

    private final InternshipProgramDictionariesCatalogService internshipProgramDictionariesCatalogService;

    @Override
    public ResponseEntity<RequirementDefinitionCatalogListResponse> getProgramRequirementDefinitionsCatalog() {
        return ResponseEntity.ok(internshipProgramDictionariesCatalogService.listRequirementDefinitions());
    }
}
