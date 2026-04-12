package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.CompetenciesApi;
import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.CompetencyCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class CompetencyCatalogController extends BaseController implements CompetenciesApi {

    private final CompetencyCatalogService competencyCatalogService;

    @Override
    public ResponseEntity<CompetencyCatalogListResponse> getCompetenciesCatalog() {
        return ResponseEntity.ok(competencyCatalogService.getListCatalog());
    }
}
