package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DepartmentsApi;
import by.bsuir.growpathserver.dto.model.CreateDepartmentRequest;
import by.bsuir.growpathserver.dto.model.DepartmentListResponse;
import by.bsuir.growpathserver.dto.model.DepartmentResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateDepartmentRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DepartmentController implements DepartmentsApi {

    @Override
    public ResponseEntity<DepartmentResponse> createDepartment(CreateDepartmentRequest createDepartmentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteDepartment(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<DepartmentResponse> getDepartmentById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<DepartmentListResponse> getDepartments() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<DepartmentResponse> updateDepartment(String id,
                                                               UpdateDepartmentRequest updateDepartmentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
