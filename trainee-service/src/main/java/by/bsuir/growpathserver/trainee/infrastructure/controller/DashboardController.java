package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DashboardApi;
import by.bsuir.growpathserver.dto.model.DashboardResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    @Override
    public ResponseEntity<DashboardResponse> getDashboard(String role) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
