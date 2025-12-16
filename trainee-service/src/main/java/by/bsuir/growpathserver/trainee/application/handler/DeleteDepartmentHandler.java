package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.DeleteDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.service.DepartmentService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteDepartmentHandler {

    private final DepartmentService departmentService;

    public void handle(DeleteDepartmentCommand command) {
        departmentService.deleteDepartment(command);
    }
}
