package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.ChangeTaskStatusRequest;
import by.bsuir.growpathserver.dto.model.CommentListResponse;
import by.bsuir.growpathserver.dto.model.CommentResponse;
import by.bsuir.growpathserver.dto.model.CreateCommentRequest;
import by.bsuir.growpathserver.dto.model.CreateTaskRequest;
import by.bsuir.growpathserver.dto.model.FileResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.ReorderTasksRequest;
import by.bsuir.growpathserver.dto.model.ReorderTasksRequestItemsInner;
import by.bsuir.growpathserver.dto.model.ReviewTaskRequest;
import by.bsuir.growpathserver.dto.model.SubmitTaskRequest;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.dto.model.TaskStatusResponse;
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
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetTaskByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.entity.TaskArtifactEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskCommentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.events.TaskReviewResultEvent;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.TaskMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskArtifactRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCommentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskFacade {
    private final CreateTaskHandler createTaskHandler;
    private final GetTasksHandler getTasksHandler;
    private final GetTaskByIdHandler getTaskByIdHandler;
    private final UpdateTaskHandler updateTaskHandler;
    private final CompleteTaskHandler completeTaskHandler;
    private final DeleteTaskHandler deleteTaskHandler;
    private final TaskMapper taskMapper;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final TaskRepository taskRepository;
    private final TaskArtifactRepository taskArtifactRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskReviewNotificationService taskReviewNotificationService;
    private final TaskArtifactStorageService taskArtifactStorageService;

    @Transactional
    public TaskStatusResponse completeTask(String id) {
        Long taskId = Long.parseLong(id);
        CompleteTaskCommand command = new CompleteTaskCommand(taskId);
        Task task = completeTaskHandler.handle(command);
        TaskStatusResponse response = new TaskStatusResponse();
        response.setId(task.getId());
        response.setStatus(TaskStatusResponse.StatusEnum.fromValue(task.getStatus().getValue()));
        response.setCompletedAt(task.getCompletedAt());
        return response;
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest createTaskRequest) {
        Long mentorId = currentApplicationUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        Long stageId = createTaskRequest.getStageId();
        Long internshipId = createTaskRequest.getInternshipId();
        LocalDateTime date = createTaskRequest.getDueDate();

        Task firstCreated = null;
        for (Long assigneeId : createTaskRequest.getAssigneeIds()) {
            CreateTaskCommand command = CreateTaskCommand.builder()
                    .title(createTaskRequest.getTitle())
                    .description(createTaskRequest.getDescription())
                    .priority(TaskPriority.fromString(createTaskRequest.getPriority().getValue()))
                    .assigneeId(assigneeId)
                    .mentorId(mentorId)
                    .internshipId(internshipId)
                    .stageId(stageId)
                    .dueDate(date)
                    .build();
            Task created = createTaskHandler.handle(command);
            if (Objects.isNull(firstCreated)) {
                firstCreated = created;
            }
        }

        if (Objects.isNull(firstCreated)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task was not created");
        }
        return enrich(taskMapper.toTaskResponse(firstCreated), firstCreated.getId());
    }

    @Transactional
    public void deleteTask(String id) {
        Long taskId = Long.parseLong(id);
        DeleteTaskCommand command = new DeleteTaskCommand(taskId);
        deleteTaskHandler.handle(command);
    }

    @Transactional
    public TaskResponse getTaskById(String id) {
        Long taskId = Long.parseLong(id);
        GetTaskByIdQuery query = new GetTaskByIdQuery(taskId);
        Task task = getTaskByIdHandler.handle(query);
        return enrich(taskMapper.toTaskResponse(task), taskId);
    }

    @Transactional
    public TaskListResponse getTasks(GetTasksQuery query) {
        Long meId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        Page<Task> tasksPage = getTasksHandler.handle(resolveGetTasksQuery(query, meId));

        TaskListResponse response = new TaskListResponse();
        response.setData(tasksPage.getContent().stream()
                                 .map(task -> enrich(taskMapper.toTaskResponse(task), task.getId()))
                                 .toList());

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(tasksPage.getNumber() + 1);
        pagination.setLimit(tasksPage.getSize());
        pagination.setTotal((int) tasksPage.getTotalElements());
        pagination.setTotalPages(tasksPage.getTotalPages());
        response.setPagination(pagination);
        return response;
    }

    @Transactional
    public TaskResponse updateTask(UpdateTaskCommand command) {
        Task task = updateTaskHandler.handle(command);
        return enrich(taskMapper.toTaskResponse(task), task.getId());
    }

    @Transactional
    public TaskStatusResponse takeTask(String id) {
        ChangeTaskStatusRequest body = new ChangeTaskStatusRequest();
        body.setAction(ChangeTaskStatusRequest.ActionEnum.TAKE);
        return changeTaskStatus(id, body);
    }

    @Transactional
    public TaskStatusResponse submitTask(String id, SubmitTaskRequest request) {
        ChangeTaskStatusRequest body = new ChangeTaskStatusRequest();
        body.setAction(ChangeTaskStatusRequest.ActionEnum.SUBMIT);
        body.setLinks(request.getLinks());
        body.setComment(request.getComment());
        return changeTaskStatus(id, body);
    }

    @Transactional
    public TaskStatusResponse reviewTask(String id, ReviewTaskRequest request) {
        ChangeTaskStatusRequest body = new ChangeTaskStatusRequest();
        if (request.getStatus() == ReviewTaskRequest.StatusEnum.COMPLETED) {
            body.setTo(ChangeTaskStatusRequest.ToEnum.COMPLETED);
            body.setRating(request.getRating());
            body.setComment(request.getComment());
        }
        else {
            body.setTo(ChangeTaskStatusRequest.ToEnum.NEEDS_REWORK);
            body.setFeedback(StringUtils.trimToNull(request.getComment()));
            body.setComment(request.getComment());
        }
        return changeTaskStatus(id, body);
    }

    @Transactional
    public TaskStatusResponse changeTaskStatus(String id, ChangeTaskStatusRequest request) {
        Long taskId = Long.parseLong(id);
        TaskEntity task = getTaskEntity(taskId);
        TaskStatus previous = task.getStatus();
        Long userId = resolveCurrentUserId();
        TaskStatus target = resolveTransitionTarget(request);

        switch (target) {
            case IN_PROGRESS -> applyTransitionToInProgress(task, userId);
            case ON_REVIEW -> applyTransitionToOnReview(task, userId, taskId, request);
            case NEEDS_REWORK -> applyTransitionToNeedsRework(task, userId, request);
            case COMPLETED -> applyTransitionToCompleted(task, userId, request);
            default -> throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transition to \"" + target.getValue() + "\" is not supported through this endpoint");
        }

        taskRepository.save(task);

        if ((previous == TaskStatus.ON_REVIEW || previous == TaskStatus.SUBMITTED)
                && (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.NEEDS_REWORK)) {
            taskReviewNotificationService.notifyReviewResult(new TaskReviewResultEvent(
                    task.getId(),
                    task.getAssigneeId(),
                    task.getStatus().getValue(),
                    task.getRating(),
                    task.getReviewComment(),
                    task.getReviewedAt()
            ));
        }

        return toStatusResponse(task);
    }

    @Transactional
    public void reorderTasks(ReorderTasksRequest body, String statusQuery) {
        Long userId = resolveCurrentUserId();
        TaskStatus expectedColumn = null;
        if (StringUtils.isNotBlank(statusQuery)) {
            expectedColumn = TaskStatus.fromString(statusQuery);
        }
        for (ReorderTasksRequestItemsInner item : body.getItems()) {
            TaskEntity task = getTaskEntity(item.getId());
            if (expectedColumn != null && task.getStatus() != expectedColumn) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Task " + task.getId() + " is not in status " + expectedColumn.getValue());
            }
            boolean allowed = Objects.equals(task.getAssigneeId(), userId) || Objects.equals(task.getMentorId(), userId);
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to reorder this task");
            }
            task.setSortOrder(item.getSortOrder());
            taskRepository.save(task);
        }
    }

    @Transactional
    public CommentResponse addTaskComment(String id, CreateCommentRequest request) {
        TaskEntity task = getTaskEntity(Long.parseLong(id));
        Long currentUserId = resolveCurrentUserId();
        TaskCommentEntity entity = new TaskCommentEntity();
        entity.setTask(task);
        entity.setUserId(currentUserId);
        entity.setComment(request.getComment());
        TaskCommentEntity saved = taskCommentRepository.save(entity);

        CommentResponse response = new CommentResponse();
        response.setId(saved.getId());
        response.setTaskId(task.getId());
        response.setUserId(saved.getUserId());
        response.setComment(saved.getComment());
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public CommentListResponse getTaskComments(String id) {
        Long taskId = Long.parseLong(id);
        getTaskEntity(taskId);
        List<Object> comments = taskCommentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream().map(c -> {
            CommentResponse response = new CommentResponse();
            response.setId(c.getId());
            response.setTaskId(taskId);
            response.setUserId(c.getUserId());
            response.setComment(c.getComment());
            response.setCreatedAt(c.getCreatedAt());
            return (Object) response;
        }).toList();
        CommentListResponse response = new CommentListResponse();
        response.setData(comments);
        return response;
    }

    private TaskResponse enrich(TaskResponse response, Long taskId) {
        List<Object> files = taskArtifactRepository.findAllByTaskId(taskId).stream().map(a -> {
            FileResponse file = new FileResponse();
            file.setId(a.getId());
            file.setName(a.getName());
            file.setType(a.getContentType());
            file.setSize(a.getSizeBytes() != null ? a.getSizeBytes().intValue() : 0);
            file.setUrl(a.getUrl());
            file.setUploadedAt(a.getUploadedAt());
            return (Object) file;
        }).toList();
        response.setSubmissionFiles(files);
        response.setSubmissionLinks(
                taskArtifactRepository.findAllByTaskId(taskId).stream().map(TaskArtifactEntity::getUrl).toList());
        response.setComments(getTaskComments(taskId.toString()).getData());
        return response;
    }

    @Transactional
    public Map<String, String> createPresignedUpload(Long taskId, String fileName) {
        TaskEntity task = getTaskEntity(taskId);
        String safeName = StringUtils.defaultIfBlank(fileName, "artifact.bin").replace(" ", "_");
        String objectKey = "tasks/%d/%d_%s".formatted(task.getId(), System.currentTimeMillis(), safeName);
        String uploadUrl = taskArtifactStorageService.createPresignedUploadUrl(objectKey);
        return Map.of("objectKey", objectKey, "uploadUrl", uploadUrl);
    }

    @Transactional
    public FileResponse confirmUploadedArtifact(Long taskId,
                                                String objectKey,
                                                String name,
                                                String contentType,
                                                Long sizeBytes) {
        TaskEntity task = getTaskEntity(taskId);
        Long currentUserId = resolveCurrentUserId();
        TaskArtifactEntity artifact = new TaskArtifactEntity();
        artifact.setTask(task);
        artifact.setArtifactType("FILE");
        artifact.setObjectKey(objectKey);
        artifact.setName(StringUtils.defaultIfBlank(name, "artifact"));
        artifact.setContentType(contentType);
        artifact.setSizeBytes(sizeBytes);
        artifact.setUploadedBy(currentUserId);
        artifact.setUrl(taskArtifactStorageService.createPresignedDownloadUrl(objectKey));
        TaskArtifactEntity saved = taskArtifactRepository.save(artifact);

        FileResponse response = new FileResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setType(saved.getContentType());
        response.setSize(saved.getSizeBytes() != null ? saved.getSizeBytes().intValue() : 0);
        response.setUrl(saved.getUrl());
        response.setUploadedAt(saved.getUploadedAt());
        return response;
    }

    private TaskStatusResponse toStatusResponse(TaskEntity task) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setId(task.getId());
        response.setStatus(TaskStatusResponse.StatusEnum.fromValue(task.getStatus().getValue()));
        response.setTakenAt(task.getTakenAt());
        response.setSubmittedAt(task.getSubmittedAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setReviewedAt(task.getReviewedAt());
        response.setReviewComment(task.getReviewComment());
        response.setRating(task.getRating() != null ? task.getRating() : null);
        response.setStatusHistory(List.of());
        return response;
    }

    private TaskEntity getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private GetTasksQuery resolveGetTasksQuery(GetTasksQuery query, Long meId) {
        if (query.internshipId() != null && "me".equalsIgnoreCase(query.internshipId().trim())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "internshipId=me is not supported");
        }
        String assignee = query.assignee();
        String mentorId = query.mentorId();
        if (query.scope() != null && "assigned_to_me".equalsIgnoreCase(query.scope().trim())) {
            assignee = String.valueOf(meId);
        }
        else if (assignee != null && "me".equalsIgnoreCase(assignee.trim())) {
            assignee = String.valueOf(meId);
        }
        if (mentorId != null && "me".equalsIgnoreCase(mentorId.trim())) {
            mentorId = String.valueOf(meId);
        }
        return GetTasksQuery.builder()
                .page(query.page())
                .limit(query.limit())
                .status(query.status())
                .assignee(assignee)
                .priority(query.priority())
                .internshipId(query.internshipId())
                .mentorId(mentorId)
                .scope(query.scope())
                .build();
    }

    private TaskStatus resolveTransitionTarget(ChangeTaskStatusRequest request) {
        if (request.getTo() != null) {
            return mapApiStatusToDomain(request.getTo());
        }
        if (request.getAction() != null) {
            return switch (request.getAction()) {
                case TAKE, START -> TaskStatus.IN_PROGRESS;
                case SUBMIT -> TaskStatus.ON_REVIEW;
                case ACCEPT, APPROVE -> TaskStatus.COMPLETED;
                case REWORK -> TaskStatus.NEEDS_REWORK;
            };
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Either \"to\" or \"action\" is required");
    }

    private static TaskStatus mapApiStatusToDomain(ChangeTaskStatusRequest.ToEnum to) {
        TaskStatus mapped = TaskStatus.fromString(to.getValue());
        if (mapped == TaskStatus.SUBMITTED) {
            return TaskStatus.ON_REVIEW;
        }
        return mapped;
    }

    private void applyTransitionToInProgress(TaskEntity task, Long userId) {
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assignee can move task to in_progress");
        }
        if (!(task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.NEEDS_REWORK)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move to in_progress from status " + task.getStatus().getValue());
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
        if (Objects.isNull(task.getTakenAt())) {
            task.setTakenAt(LocalDateTime.now());
        }
    }

    private void applyTransitionToOnReview(TaskEntity task, Long userId, Long taskId, ChangeTaskStatusRequest request) {
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assignee can submit task for review");
        }
        if (!(task.getStatus() == TaskStatus.IN_PROGRESS || task.getStatus() == TaskStatus.NEEDS_REWORK)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move to on_review from status " + task.getStatus().getValue());
        }
        boolean hasLinks = Objects.nonNull(request.getLinks()) && !request.getLinks().isEmpty();
        boolean hasArtifacts = !taskArtifactRepository.findAllByTaskId(taskId).isEmpty();
        if (!hasLinks && !hasArtifacts) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "At least one artifact link or uploaded file is required to submit for review");
        }
        if (hasLinks) {
            request.getLinks().stream().filter(StringUtils::isNotBlank).forEach(link -> {
                TaskArtifactEntity entity = new TaskArtifactEntity();
                entity.setTask(task);
                entity.setArtifactType("LINK");
                entity.setName("Link");
                entity.setUrl(link.trim());
                entity.setUploadedBy(userId);
                taskArtifactRepository.save(entity);
            });
        }
        task.setSubmissionComment(StringUtils.trimToNull(request.getComment()));
        task.setStatus(TaskStatus.ON_REVIEW);
        task.setSubmittedAt(LocalDateTime.now());
    }

    private void applyTransitionToNeedsRework(TaskEntity task, Long userId, ChangeTaskStatusRequest request) {
        if (!Objects.equals(task.getMentorId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assigned mentor can request rework");
        }
        if (!(task.getStatus() == TaskStatus.ON_REVIEW || task.getStatus() == TaskStatus.SUBMITTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move to needs_rework from status " + task.getStatus().getValue());
        }
        String feedback = StringUtils.trimToNull(request.getFeedback());
        if (StringUtils.isBlank(feedback)) {
            feedback = StringUtils.trimToNull(request.getComment());
        }
        if (StringUtils.isBlank(feedback)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Feedback is required for rework");
        }
        if (Objects.nonNull(request.getRating())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Score is not allowed for rework");
        }
        task.setStatus(TaskStatus.NEEDS_REWORK);
        task.setReviewComment(feedback);
        task.setRating(null);
        task.setReviewedAt(LocalDateTime.now());
    }

    private void applyTransitionToCompleted(TaskEntity task, Long userId, ChangeTaskStatusRequest request) {
        if (!Objects.equals(task.getMentorId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assigned mentor can complete review");
        }
        if (!(task.getStatus() == TaskStatus.ON_REVIEW || task.getStatus() == TaskStatus.SUBMITTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot move to completed from status " + task.getStatus().getValue());
        }
        if (Objects.isNull(request.getRating()) || request.getRating() < 1 || request.getRating() > 10) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Score must be an integer from 1 to 10");
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setRating(request.getRating().intValue());
        task.setReviewComment(StringUtils.trimToNull(request.getComment()));
        task.setCompletedAt(LocalDateTime.now());
        task.setReviewedAt(LocalDateTime.now());
    }

    private Long resolveCurrentUserId() {
        return currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }
}
