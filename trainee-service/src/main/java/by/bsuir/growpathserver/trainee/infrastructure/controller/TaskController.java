package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.TasksApi;
import by.bsuir.growpathserver.dto.model.CommentResponse;
import by.bsuir.growpathserver.dto.model.CreateCommentRequest;
import by.bsuir.growpathserver.dto.model.CreateTaskRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.dto.model.TaskStatusResponse;
import by.bsuir.growpathserver.dto.model.UpdateTaskRequest;
import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.application.handler.CompleteTaskHandler;
import by.bsuir.growpathserver.trainee.application.handler.CreateTaskHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteTaskHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetTaskByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetTasksHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateTaskHandler;
import by.bsuir.growpathserver.trainee.application.query.GetTaskByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController implements TasksApi {

    private final CreateTaskHandler createTaskHandler;
    private final GetTasksHandler getTasksHandler;
    private final GetTaskByIdHandler getTaskByIdHandler;
    private final UpdateTaskHandler updateTaskHandler;
    private final CompleteTaskHandler completeTaskHandler;
    private final DeleteTaskHandler deleteTaskHandler;
    private final TaskMapper taskMapper;

    @Override
    public ResponseEntity<CommentResponse> addTaskComment(String id, CreateCommentRequest createCommentRequest) {
        // TODO: Implement comment functionality
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskStatusResponse> completeTask(String id) {
        try {
            Long taskId = Long.parseLong(id);
            CompleteTaskCommand command = new CompleteTaskCommand(taskId);
            Task task = completeTaskHandler.handle(command);

            TaskStatusResponse response = new TaskStatusResponse();
            response.setId(String.valueOf(task.getId()));
            response.setStatus(TaskStatusResponse.StatusEnum.fromValue(task.getStatus().getValue()));
            response.setCompletedAt(task.getCompletedAt());

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<TaskResponse> createTask(CreateTaskRequest createTaskRequest) {
        try {
            // TODO: Get mentorId from JWT token when authentication is properly configured
            String mentorId = "mentor-id-placeholder"; // Should be extracted from JWT
            String assigneeId = createTaskRequest.getAssigneeId();
            String stageId = createTaskRequest.getStageId();
            java.time.LocalDateTime dueDate = createTaskRequest.getDueDate();

            CreateTaskCommand command = CreateTaskCommand.builder()
                    .title(createTaskRequest.getTitle())
                    .description(createTaskRequest.getDescription())
                    .priority(TaskPriority.fromString(createTaskRequest.getPriority().getValue()))
                    .assigneeId(assigneeId)
                    .mentorId(mentorId)
                    .internshipId(createTaskRequest.getInternshipId())
                    .stageId(stageId)
                    .dueDate(dueDate)
                    .build();

            Task task = createTaskHandler.handle(command);
            TaskResponse response = taskMapper.toTaskResponse(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteTask(String id) {
        try {
            Long taskId = Long.parseLong(id);
            DeleteTaskCommand command = new DeleteTaskCommand(taskId);
            deleteTaskHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("Task deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(String id) {
        try {
            Long taskId = Long.parseLong(id);
            GetTaskByIdQuery query = new GetTaskByIdQuery(taskId);
            Task task = getTaskByIdHandler.handle(query);
            TaskResponse response = taskMapper.toTaskResponse(task);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<TaskListResponse> getTasks(Integer page,
                                                     Integer limit,
                                                     String status,
                                                     String assignee,
                                                     String priority,
                                                     String internshipId,
                                                     String mentorId) {
        try {
            GetTasksQuery query = GetTasksQuery.builder()
                    .page(page)
                    .limit(limit)
                    .status(status != null ? TaskStatus.fromString(status) : null)
                    .assignee(assignee)
                    .priority(priority != null ? TaskPriority.fromString(priority) : null)
                    .internshipId(internshipId)
                    .mentorId(mentorId)
                    .build();

            Page<Task> tasksPage = getTasksHandler.handle(query);

            TaskListResponse response = new TaskListResponse();
            response.setData(tasksPage.getContent().stream()
                                     .map(taskMapper::toTaskResponse)
                                     .toList());

            PaginationResponse pagination = new PaginationResponse();
            pagination.setPage(tasksPage.getNumber() + 1);
            pagination.setLimit(tasksPage.getSize());
            pagination.setTotal((int) tasksPage.getTotalElements());
            pagination.setTotalPages(tasksPage.getTotalPages());
            response.setPagination(pagination);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<TaskResponse> updateTask(String id, UpdateTaskRequest updateTaskRequest) {
        try {
            Long taskId = Long.parseLong(id);
            String assigneeId = updateTaskRequest.getAssigneeId();
            java.time.LocalDateTime dueDate = updateTaskRequest.getDueDate();

            UpdateTaskCommand command = UpdateTaskCommand.builder()
                    .id(taskId)
                    .title(updateTaskRequest.getTitle())
                    .description(updateTaskRequest.getDescription())
                    .status(null)
                    .priority(updateTaskRequest.getPriority() != null ?
                                      TaskPriority.fromString(updateTaskRequest.getPriority().getValue()) : null)
                    .assigneeId(assigneeId)
                    .stageId(null)
                    .dueDate(dueDate)
                    .build();

            Task task = updateTaskHandler.handle(command);
            TaskResponse response = taskMapper.toTaskResponse(task);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
