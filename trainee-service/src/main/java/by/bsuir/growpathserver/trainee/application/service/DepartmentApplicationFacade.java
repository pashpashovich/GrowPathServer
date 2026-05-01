package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

@Service
@RequiredArgsConstructor
public class DepartmentApplicationFacade {

    private static final String MSG_DEPARTMENT_DELETED = "Department deleted successfully";
    private static final String MSG_INVALID_ID = "Invalid department ID format";
    private static final String MSG_DEPARTMENT_NOT_FOUND = "Department not found";

    private final CreateDepartmentHandler createDepartmentHandler;
    private final GetDepartmentsHandler getDepartmentsHandler;
    private final GetDepartmentByIdHandler getDepartmentByIdHandler;
    private final UpdateDepartmentHandler updateDepartmentHandler;
    private final DeleteDepartmentHandler deleteDepartmentHandler;
    private final DepartmentMapper departmentMapper;

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        CreateDepartmentCommand command = CreateDepartmentCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department department = createDepartmentHandler.handle(command);
        return departmentMapper.toDepartmentResponse(department);
    }

    public DepartmentListResponse getAllDepartments() {
        List<Department> departments = getDepartmentsHandler.handle();

        DepartmentListResponse response = new DepartmentListResponse();
        response.setData(departments.stream()
                                 .map(departmentMapper::toDepartmentResponse)
                                 .collect(Collectors.toList()));
        return response;
    }

    public DepartmentResponse getDepartmentById(String id) {
        Long departmentId = parseId(id);
        GetDepartmentByIdQuery query = new GetDepartmentByIdQuery(departmentId);
        Department department = getDepartmentByIdHandler.handle(query);
        return departmentMapper.toDepartmentResponse(department);
    }

    public DepartmentResponse updateDepartment(String id, UpdateDepartmentRequest request) {
        Long departmentId = parseId(id);
        UpdateDepartmentCommand command = UpdateDepartmentCommand.builder()
                .id(departmentId)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department department = updateDepartmentHandler.handle(command);
        return departmentMapper.toDepartmentResponse(department);
    }

    public MessageResponse deleteDepartment(String id) {
        Long departmentId = parseId(id);
        DeleteDepartmentCommand command = new DeleteDepartmentCommand(departmentId);
        deleteDepartmentHandler.handle(command);

        MessageResponse response = new MessageResponse();
        response.setMessage(MSG_DEPARTMENT_DELETED);
        return response;
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_INVALID_ID);
        }
    }
}
