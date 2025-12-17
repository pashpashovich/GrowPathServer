package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskEntity existingTaskEntity;
    private LocalDateTime dueDate;

    @BeforeEach
    void setUp() {
        dueDate = LocalDateTime.now().plusDays(7);
        existingTaskEntity = new TaskEntity();
        existingTaskEntity.setId(1L);
        existingTaskEntity.setTitle("Existing Task");
        existingTaskEntity.setDescription("Existing Description");
        existingTaskEntity.setStatus(TaskStatus.PENDING);
        existingTaskEntity.setPriority(TaskPriority.MEDIUM);
        existingTaskEntity.setAssigneeId(10L);
        existingTaskEntity.setMentorId(20L);
        existingTaskEntity.setInternshipId(30L);
        existingTaskEntity.setStageId(40L);
        existingTaskEntity.setDueDate(dueDate);
    }

    @Test
    void shouldCreateTaskSuccessfully() {
        // Given
        CreateTaskCommand command = CreateTaskCommand.builder()
                .title("New Task")
                .description("Task Description")
                .priority(TaskPriority.HIGH)
                .assigneeId(10L)
                .mentorId(20L)
                .internshipId(30L)
                .stageId(40L)
                .dueDate(dueDate)
                .build();

        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // When
        Task result = taskService.createTask(command);

        // Then
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals("Task Description", result.getDescription());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        // Given
        UpdateTaskCommand command = UpdateTaskCommand.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assigneeId(15L)
                .stageId(45L)
                .dueDate(dueDate.plusDays(1))
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTaskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existingTaskEntity);

        // When
        Task result = taskService.updateTask(command);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", existingTaskEntity.getTitle());
        assertEquals("Updated Description", existingTaskEntity.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, existingTaskEntity.getStatus());
        assertEquals(TaskPriority.HIGH, existingTaskEntity.getPriority());
        assertEquals(15L, existingTaskEntity.getAssigneeId());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void shouldUpdateTaskWithPartialFields() {
        // Given
        UpdateTaskCommand command = UpdateTaskCommand.builder()
                .id(1L)
                .title("Only Title Updated")
                .description(null)
                .status(null)
                .priority(null)
                .assigneeId(null)
                .stageId(null)
                .dueDate(null)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTaskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existingTaskEntity);

        // When
        Task result = taskService.updateTask(command);

        // Then
        assertNotNull(result);
        assertEquals("Only Title Updated", existingTaskEntity.getTitle());
        assertEquals("Existing Description", existingTaskEntity.getDescription()); // Should remain unchanged
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        // Given
        UpdateTaskCommand command = UpdateTaskCommand.builder()
                .id(999L)
                .title("Updated Title")
                .build();

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> taskService.updateTask(command));
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldCompleteTaskSuccessfully() {
        // Given
        existingTaskEntity.setStatus(TaskStatus.SUBMITTED);
        CompleteTaskCommand command = new CompleteTaskCommand(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTaskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(existingTaskEntity);

        // When
        Task result = taskService.completeTask(command);

        // Then
        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, existingTaskEntity.getStatus());
        assertNotNull(existingTaskEntity.getCompletedAt());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(TaskEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonSubmittedTask() {
        // Given
        existingTaskEntity.setStatus(TaskStatus.PENDING);
        CompleteTaskCommand command = new CompleteTaskCommand(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTaskEntity));

        // When & Then
        assertThrows(IllegalStateException.class, () -> taskService.completeTask(command));
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonExistentTask() {
        // Given
        CompleteTaskCommand command = new CompleteTaskCommand(999L);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> taskService.completeTask(command));
        verify(taskRepository).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        // Given
        DeleteTaskCommand command = new DeleteTaskCommand(1L);

        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(command);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        // Given
        DeleteTaskCommand command = new DeleteTaskCommand(999L);

        when(taskRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> taskService.deleteTask(command));
        verify(taskRepository).existsById(999L);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetTaskByIdSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTaskEntity));

        // When
        Task result = taskService.getTaskById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Existing Task", result.getTitle());
        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentTask() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> taskService.getTaskById(999L));
        verify(taskRepository).findById(999L);
    }
}

