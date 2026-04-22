package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.api.MeApi;
import by.bsuir.growpathserver.dto.api.TasksApi;
import by.bsuir.growpathserver.dto.model.ChangeTaskStatusRequest;
import by.bsuir.growpathserver.dto.model.CommentListResponse;
import by.bsuir.growpathserver.dto.model.CommentResponse;
import by.bsuir.growpathserver.dto.model.ConfirmTaskArtifactRequest;
import by.bsuir.growpathserver.dto.model.CreateCommentRequest;
import by.bsuir.growpathserver.dto.model.CreateTaskRequest;
import by.bsuir.growpathserver.dto.model.FileResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PresignTaskArtifactRequest;
import by.bsuir.growpathserver.dto.model.ReorderTasksRequest;
import by.bsuir.growpathserver.dto.model.ReviewTaskRequest;
import by.bsuir.growpathserver.dto.model.SubmitTaskRequest;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.dto.model.TaskStatusResponse;
import by.bsuir.growpathserver.dto.model.UpdateTaskRequest;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.application.service.TaskFacade;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TaskController extends BaseController implements TasksApi, MeApi {

    private final TaskFacade taskFacade;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<CommentResponse> addTaskComment(String id, CreateCommentRequest createCommentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskFacade.addTaskComment(id, createCommentRequest));
    }

    @Override
    public ResponseEntity<TaskStatusResponse> completeTask(String id) {
        TaskStatusResponse taskStatusResponse = taskFacade.completeTask(id);
        return ResponseEntity.ok(taskStatusResponse);
    }

    @Override
    public ResponseEntity<TaskResponse> createTask(CreateTaskRequest createTaskRequest) {
        TaskResponse task = taskFacade.createTask(createTaskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteTask(String id) {
        taskFacade.deleteTask(id);
        MessageResponse response = new MessageResponse();
        response.setMessage("Task deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(String id) {
        return ResponseEntity.ok(taskFacade.getTaskById(id));
    }

    @Override
    public ResponseEntity<CommentListResponse> getTaskComments(String id) {
        return ResponseEntity.ok(taskFacade.getTaskComments(id));
    }

    @Override
    public ResponseEntity<TaskListResponse> getTasks(Integer page,
                                                     Integer limit,
                                                     String status,
                                                     String assignee,
                                                     String priority,
                                                     String internshipId,
                                                     String mentorId,
                                                     String scope) {
        GetTasksQuery query = GetTasksQuery.builder()
                .page(page)
                .limit(limit)
                .status(status != null ? TaskStatus.fromString(status) : null)
                .assignee(assignee)
                .priority(priority != null ? TaskPriority.fromString(priority) : null)
                .internshipId(internshipId)
                .mentorId(mentorId)
                .scope(scope)
                .build();

        return ResponseEntity.ok(taskFacade.getTasks(query));
    }

    @Override
    public ResponseEntity<TaskListResponse> listMyTasks(Integer page,
                                                        Integer limit,
                                                        String status,
                                                        String priority,
                                                        String internshipId) {
        GetTasksQuery query = GetTasksQuery.builder()
                .page(page)
                .limit(limit)
                .status(status != null ? TaskStatus.fromString(status) : null)
                .assignee(null)
                .priority(priority != null ? TaskPriority.fromString(priority) : null)
                .internshipId(internshipId)
                .mentorId(null)
                .scope("assigned_to_me")
                .build();
        return ResponseEntity.ok(taskFacade.getTasks(query));
    }

    @Override
    public ResponseEntity<TaskResponse> updateTask(String id, UpdateTaskRequest updateTaskRequest) {
        Long taskId = Long.parseLong(id);
        boolean hasTransitionFieldsWithoutStatus = updateTaskRequest.getStatus() == null
                && ((updateTaskRequest.getLinks() != null && !updateTaskRequest.getLinks().isEmpty())
                || updateTaskRequest.getRating() != null
                || StringUtils.isNotBlank(updateTaskRequest.getFeedback()));
        if (hasTransitionFieldsWithoutStatus) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                                              "links, rating, and feedback require status on PUT");
        }

        boolean structural = updateTaskRequest.getTitle() != null
                || updateTaskRequest.getDescription() != null
                || updateTaskRequest.getPriority() != null
                || updateTaskRequest.getAssigneeId() != null
                || updateTaskRequest.getDueDate() != null;
        if (structural) {
            UpdateTaskCommand command = UpdateTaskCommand.builder()
                    .id(taskId)
                    .title(updateTaskRequest.getTitle())
                    .description(updateTaskRequest.getDescription())
                    .status(null)
                    .priority(updateTaskRequest.getPriority() != null ?
                                      TaskPriority.fromString(updateTaskRequest.getPriority().getValue()) : null)
                    .assigneeId(updateTaskRequest.getAssigneeId())
                    .stageId(null)
                    .dueDate(updateTaskRequest.getDueDate())
                    .build();
            taskFacade.updateTask(command);
        }

        if (updateTaskRequest.getStatus() != null) {
            ChangeTaskStatusRequest transition = new ChangeTaskStatusRequest();
            transition.setTo(ChangeTaskStatusRequest.ToEnum.fromValue(updateTaskRequest.getStatus().getValue()));
            transition.setLinks(updateTaskRequest.getLinks());
            transition.setComment(updateTaskRequest.getComment());
            transition.setRating(updateTaskRequest.getRating());
            transition.setFeedback(updateTaskRequest.getFeedback());
            taskFacade.changeTaskStatus(id, transition);
        }

        return ResponseEntity.ok(taskFacade.getTaskById(id));
    }

    @Override
    public ResponseEntity<TaskStatusResponse> patchTaskStatus(String id,
                                                              ChangeTaskStatusRequest changeTaskStatusRequest) {
        return ResponseEntity.ok(taskFacade.changeTaskStatus(id, changeTaskStatusRequest));
    }

    @Override
    public ResponseEntity<Void> reorderTasks(ReorderTasksRequest reorderTasksRequest, String status) {
        taskFacade.reorderTasks(reorderTasksRequest, status);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TaskStatusResponse> takeTask(String id) {
        return ResponseEntity.ok(taskFacade.takeTask(id));
    }

    @Override
    public ResponseEntity<TaskStatusResponse> submitTask(String id, SubmitTaskRequest submitTaskRequest) {
        return ResponseEntity.ok(taskFacade.submitTask(id, submitTaskRequest));
    }

    @Override
    public ResponseEntity<TaskStatusResponse> reviewTask(String id, ReviewTaskRequest reviewTaskRequest) {
        return ResponseEntity.ok(taskFacade.reviewTask(id, reviewTaskRequest));
    }

    @Override
    public ResponseEntity<FileResponse> uploadTaskFile(String id, MultipartFile file) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Map<String, String>> presignTaskArtifactUpload(String id,
                                                                         PresignTaskArtifactRequest request) {
        return ResponseEntity.ok(taskFacade.createPresignedUpload(Long.parseLong(id), request.getFileName()));
    }

    @Override
    public ResponseEntity<FileResponse> confirmTaskArtifactUpload(String id, ConfirmTaskArtifactRequest request) {
        FileResponse response = taskFacade.confirmUploadedArtifact(
                Long.parseLong(id),
                request.getObjectKey(),
                request.getName(),
                request.getContentType(),
                request.getSizeBytes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
