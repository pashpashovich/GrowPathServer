package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CreateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.service.DepartmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateDepartmentHandler {

    private final DepartmentService departmentService;

    public Department handle(CreateDepartmentCommand command) {
        return departmentService.createDepartment(command);
    }
}
