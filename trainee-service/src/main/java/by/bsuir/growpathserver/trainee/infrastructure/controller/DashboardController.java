package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DashboardApi;
import by.bsuir.growpathserver.dto.model.DashboardResponse;
import by.bsuir.growpathserver.trainee.application.service.ReportFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController extends BaseController implements DashboardApi {

    private final ReportFacade reportFacade;

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard(String role) {
        return ResponseEntity.ok(reportFacade.getDashboard(role));
    }
}
