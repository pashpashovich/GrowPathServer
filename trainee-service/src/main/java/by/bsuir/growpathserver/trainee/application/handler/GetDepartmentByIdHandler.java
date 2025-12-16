package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetDepartmentByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.DepartmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetDepartmentByIdHandler {

    private final DepartmentService departmentService;

    public Department handle(GetDepartmentByIdQuery query) {
        return departmentService.getDepartmentById(query.id());
    }
}
