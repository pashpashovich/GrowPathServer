package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CreateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateDepartmentCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import by.bsuir.growpathserver.trainee.domain.entity.DepartmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Department createDepartment(CreateDepartmentCommand command) {
        Department department = Department.create(
                command.name(),
                command.description()
        );

        DepartmentEntity entity = department.toEntity();
        DepartmentEntity savedEntity = departmentRepository.save(entity);
        return Department.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public Department updateDepartment(UpdateDepartmentCommand command) {
        DepartmentEntity entity = departmentRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + command.id()));

        if (command.name() != null) {
            entity.setName(command.name());
        }
        if (command.description() != null) {
            entity.setDescription(command.description());
        }

        DepartmentEntity savedEntity = departmentRepository.save(entity);
        return Department.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteDepartment(DeleteDepartmentCommand command) {
        if (!departmentRepository.existsById(command.id())) {
            throw new NoSuchElementException("Department not found with id: " + command.id());
        }
        departmentRepository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        DepartmentEntity entity = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + id));
        return Department.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(Department::fromEntity)
                .toList();
    }
}
