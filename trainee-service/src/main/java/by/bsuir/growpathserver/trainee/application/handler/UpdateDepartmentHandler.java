package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.UpdateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.service.DepartmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateDepartmentHandler {

    private final DepartmentService departmentService;

    public Department handle(UpdateDepartmentCommand command) {
        return departmentService.updateDepartment(command);
    }
}
