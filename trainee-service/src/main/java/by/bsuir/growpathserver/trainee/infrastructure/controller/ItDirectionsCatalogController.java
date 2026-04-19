package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ItDirectionsApi;
import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramDictionariesCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class ItDirectionsCatalogController extends BaseController implements ItDirectionsApi {

    private final InternshipProgramDictionariesCatalogService internshipProgramDictionariesCatalogService;

    @Override
    public ResponseEntity<ItDirectionCatalogListResponse> getItDirectionsCatalog() {
        return ResponseEntity.ok(internshipProgramDictionariesCatalogService.listItDirections());
    }
}
