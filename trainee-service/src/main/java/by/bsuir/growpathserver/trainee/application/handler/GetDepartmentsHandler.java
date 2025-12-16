package by.bsuir.growpathserver.trainee.application.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.service.DepartmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetDepartmentsHandler {

    private final DepartmentService departmentService;

    public List<Department> handle() {
        return departmentService.getAllDepartments();
    }
}
