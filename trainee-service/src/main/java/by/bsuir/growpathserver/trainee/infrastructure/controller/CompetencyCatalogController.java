package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.CompetenciesApi;
import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.dto.model.CreateCompetencyRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateCompetencyRequest;
import by.bsuir.growpathserver.trainee.application.service.CompetencyCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class CompetencyCatalogController extends BaseController implements CompetenciesApi {

    private final CompetencyCatalogService competencyCatalogService;

    @Override
    public ResponseEntity<CompetencyRef> createCompetency(CreateCompetencyRequest createCompetencyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(competencyCatalogService.create(createCompetencyRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteCompetency(String id) {
        return ResponseEntity.ok(competencyCatalogService.delete(id));
    }

    @Override
    public ResponseEntity<CompetencyRef> getCompetencyById(String id) {
        return ResponseEntity.ok(competencyCatalogService.getById(id));
    }

    @Override
    public ResponseEntity<CompetencyCatalogListResponse> getCompetenciesCatalog() {
        return ResponseEntity.ok(competencyCatalogService.getListCatalog());
    }

    @Override
    public ResponseEntity<CompetencyRef> updateCompetency(String id, UpdateCompetencyRequest updateCompetencyRequest) {
        return ResponseEntity.ok(competencyCatalogService.update(id, updateCompetencyRequest));
    }
}
