package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CreateDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteDepartmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateDepartmentCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Department;
import by.bsuir.growpathserver.trainee.domain.entity.DepartmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private DepartmentEntity existingDepartmentEntity;

    @BeforeEach
    void setUp() {
        existingDepartmentEntity = new DepartmentEntity();
        existingDepartmentEntity.setId(1L);
        existingDepartmentEntity.setName("Engineering");
        existingDepartmentEntity.setDescription("Software Engineering Department");
    }

    @Test
    void shouldCreateDepartmentSuccessfully() {
        // Given
        CreateDepartmentCommand command = CreateDepartmentCommand.builder()
                .name("Marketing")
                .description("Marketing Department")
                .build();

        when(departmentRepository.save(any(DepartmentEntity.class))).thenAnswer(invocation -> {
            DepartmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        Department result = departmentService.createDepartment(command);

        // Then
        assertNotNull(result);
        assertEquals("Marketing", result.getName());
        assertEquals("Marketing Department", result.getDescription());
        verify(departmentRepository).save(any(DepartmentEntity.class));
    }

    @Test
    void shouldCreateDepartmentWithNullDescription() {
        // Given
        CreateDepartmentCommand command = CreateDepartmentCommand.builder()
                .name("HR")
                .description(null)
                .build();

        when(departmentRepository.save(any(DepartmentEntity.class))).thenAnswer(invocation -> {
            DepartmentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        Department result = departmentService.createDepartment(command);

        // Then
        assertNotNull(result);
        assertEquals("HR", result.getName());
        verify(departmentRepository).save(any(DepartmentEntity.class));
    }

    @Test
    void shouldUpdateDepartmentSuccessfully() {
        // Given
        UpdateDepartmentCommand command = UpdateDepartmentCommand.builder()
                .id(1L)
                .name("Updated Engineering")
                .description("Updated Description")
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existingDepartmentEntity));
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(existingDepartmentEntity);

        // When
        Department result = departmentService.updateDepartment(command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Engineering", existingDepartmentEntity.getName());
        assertEquals("Updated Description", existingDepartmentEntity.getDescription());
        verify(departmentRepository).findById(1L);
        verify(departmentRepository).save(any(DepartmentEntity.class));
    }

    @Test
    void shouldUpdateDepartmentWithPartialFields() {
        // Given
        UpdateDepartmentCommand command = UpdateDepartmentCommand.builder()
                .id(1L)
                .name("Only Name Updated")
                .description(null)
                .build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existingDepartmentEntity));
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(existingDepartmentEntity);

        // When
        Department result = departmentService.updateDepartment(command);

        // Then
        assertNotNull(result);
        assertEquals("Only Name Updated", existingDepartmentEntity.getName());
        assertEquals("Software Engineering Department",
                     existingDepartmentEntity.getDescription());
        verify(departmentRepository).findById(1L);
        verify(departmentRepository).save(any(DepartmentEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDepartment() {
        // Given
        UpdateDepartmentCommand command = UpdateDepartmentCommand.builder()
                .id(999L)
                .name("Updated Name")
                .build();

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> departmentService.updateDepartment(command));
        verify(departmentRepository).findById(999L);
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteDepartmentSuccessfully() {
        // Given
        DeleteDepartmentCommand command = new DeleteDepartmentCommand(1L);

        when(departmentRepository.existsById(1L)).thenReturn(true);

        // When
        departmentService.deleteDepartment(command);

        // Then
        verify(departmentRepository).existsById(1L);
        verify(departmentRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDepartment() {
        // Given
        DeleteDepartmentCommand command = new DeleteDepartmentCommand(999L);

        when(departmentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> departmentService.deleteDepartment(command));
        verify(departmentRepository).existsById(999L);
        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetDepartmentByIdSuccessfully() {
        // Given
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existingDepartmentEntity));

        // When
        Department result = departmentService.getDepartmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());
        assertEquals("Software Engineering Department", result.getDescription());
        verify(departmentRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentDepartment() {
        // Given
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> departmentService.getDepartmentById(999L));
        verify(departmentRepository).findById(999L);
    }

    @Test
    void shouldGetAllDepartmentsSuccessfully() {
        // Given
        DepartmentEntity dept1 = new DepartmentEntity();
        dept1.setId(1L);
        dept1.setName("Engineering");
        dept1.setDescription("Engineering Department");

        DepartmentEntity dept2 = new DepartmentEntity();
        dept2.setId(2L);
        dept2.setName("Marketing");
        dept2.setDescription("Marketing Department");

        when(departmentRepository.findAll()).thenReturn(List.of(dept1, dept2));

        // When
        List<Department> result = departmentService.getAllDepartments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Engineering", result.get(0).getName());
        assertEquals("Marketing", result.get(1).getName());
        verify(departmentRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoDepartmentsExist() {
        // Given
        when(departmentRepository.findAll()).thenReturn(List.of());

        // When
        List<Department> result = departmentService.getAllDepartments();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(departmentRepository).findAll();
    }
}
