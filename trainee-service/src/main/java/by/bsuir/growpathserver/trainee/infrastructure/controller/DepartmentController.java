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
import by.bsuir.growpathserver.trainee.application.service.DepartmentApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DepartmentController extends BaseController implements DepartmentsApi {

    private final DepartmentApplicationFacade departmentApplicationFacade;

    @Override
    public ResponseEntity<DepartmentResponse> createDepartment(CreateDepartmentRequest createDepartmentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentApplicationFacade.createDepartment(createDepartmentRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteDepartment(String id) {
        return ResponseEntity.ok(departmentApplicationFacade.deleteDepartment(id));
    }

    @Override
    public ResponseEntity<DepartmentResponse> getDepartmentById(String id) {
        return ResponseEntity.ok(departmentApplicationFacade.getDepartmentById(id));
    }

    @Override
    public ResponseEntity<DepartmentListResponse> getDepartments() {
        return ResponseEntity.ok(departmentApplicationFacade.getAllDepartments());
    }

    @Override
    public ResponseEntity<DepartmentResponse> updateDepartment(String id,
                                                               UpdateDepartmentRequest updateDepartmentRequest) {
        return ResponseEntity.ok(departmentApplicationFacade.updateDepartment(id, updateDepartmentRequest));
    }
}
