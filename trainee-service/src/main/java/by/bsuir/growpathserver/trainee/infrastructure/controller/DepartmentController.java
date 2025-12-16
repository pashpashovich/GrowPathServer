package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DepartmentsApi;
import by.bsuir.growpathserver.dto.model.CreateDepartmentRequest;
import by.bsuir.growpathserver.dto.model.DepartmentListResponse;
import by.bsuir.growpathserver.dto.model.DepartmentResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateDepartmentRequest;
import by.bsuir.growpathserver.trainee.application.command.CreateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateDepartmentHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteDepartmentHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetDepartmentByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetDepartmentsHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateDepartmentHandler;
import by.bsuir.growpathserver.trainee.application.query.GetDepartmentByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DepartmentController implements DepartmentsApi {

    private final CreateDepartmentHandler createDepartmentHandler;
    private final GetDepartmentsHandler getDepartmentsHandler;
    private final GetDepartmentByIdHandler getDepartmentByIdHandler;
    private final UpdateDepartmentHandler updateDepartmentHandler;
    private final DeleteDepartmentHandler deleteDepartmentHandler;
    private final DepartmentMapper departmentMapper;

    @Override
    public ResponseEntity<DepartmentResponse> createDepartment(CreateDepartmentRequest createDepartmentRequest) {
        try {
            CreateDepartmentCommand command = CreateDepartmentCommand.builder()
                    .name(createDepartmentRequest.getName())
                    .description(createDepartmentRequest.getDescription())
                    .build();

            Department department = createDepartmentHandler.handle(command);
            DepartmentResponse response = departmentMapper.toDepartmentResponse(department);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteDepartment(String id) {
        try {
            Long departmentId = Long.parseLong(id);
            DeleteDepartmentCommand command = new DeleteDepartmentCommand(departmentId);
            deleteDepartmentHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("Department deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<DepartmentResponse> getDepartmentById(String id) {
        try {
            Long departmentId = Long.parseLong(id);
            GetDepartmentByIdQuery query = new GetDepartmentByIdQuery(departmentId);
            Department department = getDepartmentByIdHandler.handle(query);
            DepartmentResponse response = departmentMapper.toDepartmentResponse(department);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<DepartmentListResponse> getDepartments() {
        try {
            var departments = getDepartmentsHandler.handle();
            DepartmentListResponse response = new DepartmentListResponse();
            response.setData(departments.stream()
                                     .map(departmentMapper::toDepartmentResponse)
                                     .collect(java.util.stream.Collectors.toList()));
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DepartmentResponse> updateDepartment(String id,
                                                               UpdateDepartmentRequest updateDepartmentRequest) {
        try {
            Long departmentId = Long.parseLong(id);
            UpdateDepartmentCommand command = UpdateDepartmentCommand.builder()
                    .id(departmentId)
                    .name(updateDepartmentRequest.getName())
                    .description(updateDepartmentRequest.getDescription())
                    .build();

            Department department = updateDepartmentHandler.handle(command);
            DepartmentResponse response = departmentMapper.toDepartmentResponse(department);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
