package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ItDirectionsApi;
import by.bsuir.growpathserver.dto.model.CreateItDirectionRequest;
import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ItDirectionRef;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateItDirectionRequest;
import by.bsuir.growpathserver.trainee.application.service.ItDirectionCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
public class ItDirectionsCatalogController extends BaseController implements ItDirectionsApi {

    private final ItDirectionCatalogService itDirectionCatalogService;

    @Override
    public ResponseEntity<ItDirectionRef> createItDirection(CreateItDirectionRequest createItDirectionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itDirectionCatalogService.create(createItDirectionRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteItDirection(String id) {
        return ResponseEntity.ok(itDirectionCatalogService.delete(id));
    }

    @Override
    public ResponseEntity<ItDirectionRef> getItDirectionById(String id) {
        return ResponseEntity.ok(itDirectionCatalogService.getById(id));
    }

    @Override
    public ResponseEntity<ItDirectionCatalogListResponse> getItDirectionsCatalog() {
        return ResponseEntity.ok(itDirectionCatalogService.list());
    }

    @Override
    public ResponseEntity<ItDirectionRef> updateItDirection(String id,
                                                            UpdateItDirectionRequest updateItDirectionRequest) {
        return ResponseEntity.ok(itDirectionCatalogService.update(id, updateItDirectionRequest));
    }
}
