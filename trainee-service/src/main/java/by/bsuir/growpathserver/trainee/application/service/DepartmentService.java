package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;

import by.bsuir.growpathserver.trainee.application.command.CreateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateDepartmentCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;

public interface DepartmentService {
    Department createDepartment(CreateDepartmentCommand command);

    Department updateDepartment(UpdateDepartmentCommand command);

    void deleteDepartment(DeleteDepartmentCommand command);

    Department getDepartmentById(Long id);

    List<Department> getAllDepartments();
}
