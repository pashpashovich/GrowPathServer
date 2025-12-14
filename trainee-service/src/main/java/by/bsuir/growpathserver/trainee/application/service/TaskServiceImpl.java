package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public Task createTask(CreateTaskCommand command) {
        Task task = Task.create(
                command.title(),
                command.description(),
                command.priority(),
                command.mentorId(),
                command.internshipId(),
                command.stageId(),
                command.assigneeId(),
                command.dueDate()
        );

        TaskEntity entity = task.toEntity();
        TaskEntity savedEntity = taskRepository.save(entity);
        return Task.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public Task updateTask(UpdateTaskCommand command) {
        TaskEntity entity = taskRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + command.id()));

        if (command.title() != null) {
            entity.setTitle(command.title());
        }
        if (command.description() != null) {
            entity.setDescription(command.description());
        }
        if (command.status() != null) {
            entity.setStatus(command.status());
        }
        if (command.priority() != null) {
            entity.setPriority(command.priority());
        }
        if (command.assigneeId() != null) {
            entity.setAssigneeId(command.assigneeId());
        }
        if (command.stageId() != null) {
            entity.setStageId(command.stageId());
        }
        if (command.dueDate() != null) {
            entity.setDueDate(command.dueDate());
        }

        TaskEntity savedEntity = taskRepository.save(entity);
        return Task.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public Task completeTask(CompleteTaskCommand command) {
        TaskEntity entity = taskRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + command.id()));

        if (entity.getStatus() != TaskStatus.SUBMITTED) {
            throw new IllegalStateException("Task must be in submitted status to be completed");
        }

        entity.setStatus(TaskStatus.COMPLETED);
        entity.setCompletedAt(LocalDateTime.now());

        TaskEntity savedEntity = taskRepository.save(entity);
        return Task.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteTask(DeleteTaskCommand command) {
        if (!taskRepository.existsById(command.id())) {
            throw new NoSuchElementException("Task not found with id: " + command.id());
        }
        taskRepository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(String id) {
        TaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
        return Task.fromEntity(entity);
    }
}
