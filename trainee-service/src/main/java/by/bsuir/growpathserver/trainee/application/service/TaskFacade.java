package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.ChangeTaskStatusRequest;
import by.bsuir.growpathserver.dto.model.CommentListResponse;
import by.bsuir.growpathserver.dto.model.CommentResponse;
import by.bsuir.growpathserver.dto.model.CreateCommentRequest;
import by.bsuir.growpathserver.dto.model.CreateTaskAssignment;
import by.bsuir.growpathserver.dto.model.CreateTaskRequest;
import by.bsuir.growpathserver.dto.model.FileResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.ReorderTasksRequest;
import by.bsuir.growpathserver.dto.model.ReorderTasksRequestItemsInner;
import by.bsuir.growpathserver.dto.model.ReviewTaskRequest;
import by.bsuir.growpathserver.dto.model.SubmitTaskRequest;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.TaskRecommendationResponse;
import by.bsuir.growpathserver.dto.model.TaskResponse;
import by.bsuir.growpathserver.dto.model.TaskStatusResponse;
import by.bsuir.growpathserver.dto.model.UpdateTaskRequest;
import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.application.dto.TaskRecommendationDto;
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
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.TaskArtifactEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskCommentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.events.TaskReviewResultEvent;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.TaskMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprStageRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskArtifactRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCommentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
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
    private final TaskRecommendationService taskRecommendationService;
    private final TaskIprStageBindingService taskIprStageBindingService;
    private final TaskCompetencyBindingService taskCompetencyBindingService;
    private final IprStageRepository iprStageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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
        Long internshipId = createTaskRequest.getInternshipId();
        Long iprId = createTaskRequest.getIprId();
        LocalDateTime date = createTaskRequest.getDueDate();
        List<InternStageAssignment> assignments = resolveAssignments(createTaskRequest);

        Task firstCreated = null;
        for (InternStageAssignment assignment : assignments) {
            taskIprStageBindingService.validateStageBinding(
                    assignment.iprStageId(), iprId, internshipId, assignment.internId());
            CreateTaskCommand command = CreateTaskCommand.builder()
                    .title(createTaskRequest.getTitle())
                    .description(createTaskRequest.getDescription())
                    .priority(TaskPriority.fromString(createTaskRequest.getPriority().getValue()))
                    .assigneeId(assignment.internId())
                    .mentorId(mentorId)
                    .internshipId(internshipId)
                    .stageId(assignment.iprStageId())
                    .dueDate(date)
                    .build();
            Task created = createTaskHandler.handle(command);
            bindTaskCompetenciesIfPresent(created.getId(), internshipId, createTaskRequest.getCompetencyIds());
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

    @Transactional(readOnly = true)
    public TaskListResponse getTasks(GetTasksQuery query) {
        Long meId = currentApplicationUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        return toTaskListResponse(getTasksHandler.handle(resolveGetTasksQuery(query, meId)));
    }

    @Transactional(readOnly = true)
    public TaskListResponse getTaskProfile(GetTasksQuery filters) {
        Long userId = resolveCurrentUserId();
        GetTasksQuery query = buildProfileScopedQuery(filters, userId);
        return toTaskListResponse(getTasksHandler.handle(query));
    }

    @Transactional
    public TaskResponse updateTask(UpdateTaskCommand command) {
        Task task = updateTaskHandler.handle(command);
        return enrich(taskMapper.toTaskResponse(task), task.getId());
    }

    @Transactional
    public TaskResponse updateTaskStructure(Long taskId, UpdateTaskRequest request) {
        TaskEntity existing = getTaskEntity(taskId);
        Long assigneeId = Objects.nonNull(request.getAssigneeId()) ? request.getAssigneeId() : existing.getAssigneeId();
        Long resolvedStageId = taskIprStageBindingService.resolveStageId(request.getIprStageId(), request.getStageId());
        if (Objects.nonNull(resolvedStageId) || Objects.nonNull(request.getIprId())) {
            taskIprStageBindingService.validateStageBinding(
                    resolvedStageId, request.getIprId(), existing.getInternshipId(), assigneeId);
        }
        UpdateTaskCommand command = UpdateTaskCommand.builder()
                .id(taskId)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(null)
                .priority(Objects.nonNull(request.getPriority()) ?
                                  TaskPriority.fromString(request.getPriority().getValue()) : null)
                .assigneeId(request.getAssigneeId())
                .stageId(resolvedStageId)
                .dueDate(request.getDueDate())
                .build();
        TaskResponse response = updateTask(command);
        if (Objects.nonNull(request.getCompetencyIds())) {
            taskCompetencyBindingService.replaceTaskCompetencies(
                    taskId, existing.getInternshipId(), request.getCompetencyIds());
            response = enrich(response, taskId);
        }
        return response;
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
        ChangeTaskStatusRequest body = buildStatusChangeRequest(request);
        return changeTaskStatus(id, body);
    }

    @Transactional
    public TaskStatusResponse changeTaskStatus(String id, ChangeTaskStatusRequest request) {
        Long taskId = Long.parseLong(id);
        TaskEntity task = getTaskEntity(taskId);
        TaskStatus previous = task.getStatus();
        Long userId = resolveCurrentUserId();
        TaskStatus target = resolveTransitionTarget(request);

        applyTransition(task, userId, target, request);
        taskRepository.save(task);
        sendReviewNotificationIfNeeded(task, previous);

        return toStatusResponse(task);
    }

    private ChangeTaskStatusRequest buildStatusChangeRequest(ReviewTaskRequest request) {
        ChangeTaskStatusRequest body = new ChangeTaskStatusRequest();

        if (ReviewTaskRequest.StatusEnum.COMPLETED.equals(request.getStatus())) {
            body.setTo(ChangeTaskStatusRequest.ToEnum.COMPLETED);
            body.setRating(request.getRating());
        }
        else {
            body.setTo(ChangeTaskStatusRequest.ToEnum.NEEDS_REWORK);
            body.setFeedback(StringUtils.trimToNull(request.getComment()));
        }

        body.setComment(request.getComment());
        return body;
    }

    private void applyTransition(TaskEntity task, Long userId, TaskStatus target,
                                 ChangeTaskStatusRequest request) {
        switch (target) {
            case PENDING -> applyTransitionToPending(task, userId);
            case IN_PROGRESS -> applyTransitionToInProgress(task, userId);
            case ON_REVIEW -> applyTransitionToOnReview(task, userId, request);
            case NEEDS_REWORK -> applyTransitionToNeedsRework(task, userId, request);
            case COMPLETED -> applyTransitionToCompleted(task, userId, request);
            case REJECTED -> applyTransitionToRejected(task, userId, request);
            default -> throw unsupportedTransition(target);
        }
    }

    private ResponseStatusException unsupportedTransition(TaskStatus target) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                                           "Transition to \"" + target.getValue()
                                                   + "\" is not supported through this endpoint");
    }

    private void sendReviewNotificationIfNeeded(TaskEntity task, TaskStatus previous) {
        if (isReviewCompleted(previous, task.getStatus())) {
            taskReviewNotificationService.notifyReviewResult(
                    new TaskReviewResultEvent(
                            task.getId(),
                            task.getAssigneeId(),
                            task.getStatus().getValue(),
                            task.getRating(),
                            task.getReviewComment(),
                            task.getReviewedAt()
                    )
            );
        }
    }

    private boolean isReviewCompleted(TaskStatus previous, TaskStatus current) {
        return (TaskStatus.ON_REVIEW.equals(previous) || TaskStatus.SUBMITTED.equals(previous))
                && (TaskStatus.COMPLETED.equals(current) || TaskStatus.NEEDS_REWORK.equals(current));
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
                                                  "Task " + task.getId() + " is not in status "
                                                          + expectedColumn.getValue());
            }
            boolean allowed =
                    Objects.equals(task.getAssigneeId(), userId) || Objects.equals(task.getMentorId(), userId);
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
        return enrich(response, taskId, Map.of());
    }

    private List<InternStageAssignment> resolveAssignments(CreateTaskRequest createTaskRequest) {
        if (CollectionUtils.isNotEmpty(createTaskRequest.getAssignments())) {
            if (CollectionUtils.isNotEmpty(createTaskRequest.getAssigneeIds())
                    || Objects.nonNull(createTaskRequest.getAssigneeId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                  "Use assignments or assigneeId/assigneeIds, not both");
            }
            List<InternStageAssignment> result = new ArrayList<>();
            Set<Long> seenInternIds = new HashSet<>();
            for (Object assignmentItem : createTaskRequest.getAssignments()) {
                CreateTaskAssignment assignment = parseAssignment(assignmentItem);
                if (Objects.isNull(assignment.getInternId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                      "internId is required in each assignment");
                }
                if (!seenInternIds.add(assignment.getInternId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                      "Duplicate internId in assignments");
                }
                result.add(new InternStageAssignment(
                        assignment.getInternId(),
                        taskIprStageBindingService.resolveStageId(
                                assignment.getIprStageId(), assignment.getStageId())));
            }
            return result;
        }

        Long sharedStageId = taskIprStageBindingService.resolveStageId(
                createTaskRequest.getIprStageId(), createTaskRequest.getStageId());
        List<Long> assigneeIds = resolveLegacyAssigneeIds(createTaskRequest);
        return assigneeIds.stream()
                .map(assigneeId -> new InternStageAssignment(assigneeId, sharedStageId))
                .toList();
    }

    private List<Long> resolveLegacyAssigneeIds(CreateTaskRequest createTaskRequest) {
        if (CollectionUtils.isNotEmpty(createTaskRequest.getAssigneeIds())) {
            return createTaskRequest.getAssigneeIds();
        }
        if (Objects.nonNull(createTaskRequest.getAssigneeId())) {
            return List.of(createTaskRequest.getAssigneeId());
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                          "assignments or assigneeId/assigneeIds is required");
    }

    private record InternStageAssignment(Long internId, Long iprStageId) {
    }

    private CreateTaskAssignment parseAssignment(Object item) {
        if (item instanceof CreateTaskAssignment assignment) {
            return assignment;
        }
        return objectMapper.convertValue(item, CreateTaskAssignment.class);
    }

    private void applyIprId(TaskResponse response) {
        if (Objects.isNull(response.getStageId())) {
            return;
        }
        iprStageRepository.findLoadedById(response.getStageId())
                .map(stage -> stage.getIpr())
                .filter(Objects::nonNull)
                .ifPresent(ipr -> response.setIprId(ipr.getId()));
    }

    private TaskResponse enrich(TaskResponse response, Long taskId, Map<Long, String> userNames) {
        response.setAssigneeName(resolveUserDisplayName(response.getAssigneeId(), userNames));
        response.setMentorName(resolveUserDisplayName(response.getMentorId(), userNames));
        applyIprId(response);

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
        response.setCompetencyRefs(new ArrayList<>(taskCompetencyBindingService.getCompetencyRefsForTask(taskId)));
        return response;
    }

    private void bindTaskCompetenciesIfPresent(Long taskId, Long programId, List<Long> competencyIds) {
        if (CollectionUtils.isEmpty(competencyIds)) {
            return;
        }
        taskCompetencyBindingService.replaceTaskCompetencies(taskId, programId, competencyIds);
    }

    @Transactional
    public FileResponse uploadTaskFile(Long taskId, MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        TaskEntity task = getTaskEntity(taskId);
        String originalName = StringUtils.defaultIfBlank(file.getOriginalFilename(), "artifact.bin");
        String safeName = originalName.replace(" ", "_");
        String objectKey = "tasks/%d/%d_%s".formatted(task.getId(), System.currentTimeMillis(), safeName);
        try {
            taskArtifactStorageService.upload(
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    StringUtils.defaultIfBlank(file.getContentType(), "application/octet-stream"));
        }
        catch (java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file");
        }
        return confirmUploadedArtifact(
                taskId,
                objectKey,
                originalName,
                file.getContentType(),
                file.getSize());
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

    private Map<Long, String> loadUserDisplayNames(List<Task> tasks) {
        Set<Long> userIds = new HashSet<>();
        for (Task task : tasks) {
            if (task.getAssigneeId() != null) {
                userIds.add(task.getAssigneeId());
            }
            if (task.getMentorId() != null) {
                userIds.add(task.getMentorId());
            }
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, entity -> User.fromEntity(entity).getDisplayName()));
    }

    private String resolveUserDisplayName(Long userId, Map<Long, String> cachedNames) {
        if (userId == null) {
            return null;
        }
        if (cachedNames.containsKey(userId)) {
            return cachedNames.get(userId);
        }
        return userRepository.findById(userId)
                .map(entity -> User.fromEntity(entity).getDisplayName())
                .orElse(null);
    }

    private TaskListResponse toTaskListResponse(Page<Task> tasksPage) {
        List<Task> tasks = tasksPage.getContent();
        Map<Long, String> userNames = loadUserDisplayNames(tasks);

        TaskListResponse response = new TaskListResponse();
        response.setData(tasks.stream()
                                 .map(task -> enrich(taskMapper.toTaskResponse(task), task.getId(), userNames))
                                 .toList());

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(tasksPage.getNumber() + 1);
        pagination.setLimit(tasksPage.getSize());
        pagination.setTotal((int) tasksPage.getTotalElements());
        pagination.setTotalPages(tasksPage.getTotalPages());
        response.setPagination(pagination);
        return response;
    }

    private GetTasksQuery buildProfileScopedQuery(GetTasksQuery filters, Long userId) {
        var builder = GetTasksQuery.builder()
                .page(filters.page())
                .limit(filters.limit())
                .status(filters.status())
                .priority(filters.priority())
                .internshipId(filters.internshipId());

        if (isIntern()) {
            builder.assignee(String.valueOf(userId));
        }
        else if (isMentor()) {
            builder.mentorId(String.valueOf(userId));
        }
        else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only mentors and interns can use this endpoint");
        }
        return builder.build();
    }

    private GetTasksQuery resolveGetTasksQuery(GetTasksQuery query, Long meId) {
        if (query.internshipId() != null && "me".equalsIgnoreCase(query.internshipId().trim())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "internshipId=me is not supported");
        }
        String assignee = query.assignee();
        String mentorId = query.mentorId();
        if (query.scope() != null && "assigned_to_me".equalsIgnoreCase(query.scope().trim())) {
            if (meId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
            assignee = String.valueOf(meId);
        }
        else if (assignee != null && meId != null && "me".equalsIgnoreCase(assignee.trim())) {
            assignee = String.valueOf(meId);
        }
        if (mentorId != null && meId != null && "me".equalsIgnoreCase(mentorId.trim())) {
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

    private boolean isMentor() {
        return hasAuthority("MENTOR");
    }

    private boolean isIntern() {
        return hasAuthority("INTERN");
    }

    private boolean hasAuthority(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
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

    private void applyTransitionToPending(TaskEntity task, Long userId) {
        if (!canMentorReopenFromCompleted(task, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Cannot move to pending from status " + task.getStatus().getValue());
        }
        task.setStatus(TaskStatus.PENDING);
        task.setTakenAt(null);
        task.setSubmittedAt(null);
        task.setCompletedAt(null);
        task.setReviewedAt(null);
        task.setRating(null);
        task.setReviewComment(null);
    }

    private void applyTransitionToRejected(TaskEntity task, Long userId, ChangeTaskStatusRequest request) {
        if (!canMentorReopenFromCompleted(task, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Cannot move to rejected from status " + task.getStatus().getValue());
        }
        task.setStatus(TaskStatus.REJECTED);
        task.setCompletedAt(null);
        task.setRating(null);
        task.setReviewedAt(LocalDateTime.now());
        String comment = StringUtils.trimToNull(request.getFeedback());
        if (StringUtils.isBlank(comment)) {
            comment = StringUtils.trimToNull(request.getComment());
        }
        task.setReviewComment(comment);
    }

    private void applyTransitionToOnReview(TaskEntity task, Long userId, ChangeTaskStatusRequest request) {
        if (canMentorReopenFromCompleted(task, userId)) {
            applyMentorReopenToOnReview(task, request);
            return;
        }
        if (!Objects.equals(task.getAssigneeId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only assignee can submit task for review");
        }
        if (!(TaskStatus.IN_PROGRESS.equals(task.getStatus()) || TaskStatus.NEEDS_REWORK.equals(task.getStatus()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Cannot move to on_review from status " + task.getStatus().getValue());
        }
        task.setStatus(TaskStatus.ON_REVIEW);
        task.setSubmittedAt(LocalDateTime.now());
        task.setSubmissionComment(StringUtils.trimToNull(request.getComment()));
        persistSubmissionLinks(task, userId, request.getLinks());
    }

    private void persistSubmissionLinks(TaskEntity task, Long userId, List<String> links) {
        if (CollectionUtils.isEmpty(links)) {
            return;
        }
        for (String link : links) {
            if (StringUtils.isBlank(link)) {
                continue;
            }
            String trimmed = link.trim();
            TaskArtifactEntity artifact = new TaskArtifactEntity();
            artifact.setTask(task);
            artifact.setArtifactType("LINK");
            artifact.setName(trimmed);
            artifact.setUrl(trimmed);
            artifact.setUploadedBy(userId);
            taskArtifactRepository.save(artifact);
        }
    }

    private void applyTransitionToNeedsRework(TaskEntity task, Long userId, ChangeTaskStatusRequest request) {
        if (canMentorReopenFromCompleted(task, userId)) {
            applyMentorReopenToNeedsRework(task, request);
            return;
        }
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

    private boolean canMentorReopenFromCompleted(TaskEntity task, Long userId) {
        return Objects.equals(task.getMentorId(), userId) && TaskStatus.COMPLETED.equals(task.getStatus());
    }

    private void applyMentorReopenToOnReview(TaskEntity task, ChangeTaskStatusRequest request) {
        task.setStatus(TaskStatus.ON_REVIEW);
        task.setCompletedAt(null);
        task.setRating(null);
        if (Objects.isNull(task.getSubmittedAt())) {
            task.setSubmittedAt(LocalDateTime.now());
        }
        String comment = StringUtils.trimToNull(request.getComment());
        if (StringUtils.isNotBlank(comment)) {
            task.setSubmissionComment(comment);
        }
    }

    private void applyMentorReopenToNeedsRework(TaskEntity task, ChangeTaskStatusRequest request) {
        task.setStatus(TaskStatus.NEEDS_REWORK);
        task.setCompletedAt(null);
        task.setRating(null);
        task.setReviewedAt(LocalDateTime.now());
        String feedback = StringUtils.trimToNull(request.getFeedback());
        if (StringUtils.isBlank(feedback)) {
            feedback = StringUtils.trimToNull(request.getComment());
        }
        if (StringUtils.isNotBlank(feedback)) {
            task.setReviewComment(feedback);
        }
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

    @Transactional(readOnly = true)
    public List<TaskRecommendationResponse> getRecommendedTasks(int limit) {
        Long internId = resolveCurrentUserId();
        List<TaskRecommendationDto> recommendations = taskRecommendationService.getRecommendedTasks(internId, limit);

        return recommendations.stream()
                .map(this::toTaskRecommendationResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private TaskRecommendationResponse toTaskRecommendationResponse(TaskRecommendationDto dto) {
        TaskRecommendationResponse response = new TaskRecommendationResponse();
        response.setTaskId(dto.getTaskId());
        response.setTitle(dto.getTitle());
        response.setDescription(dto.getDescription());
        response.setPriority(TaskRecommendationResponse.PriorityEnum.fromValue(dto.getPriority()));
        response.setDueDate(dto.getDueDate());
        response.setRelevanceScore(dto.getRelevanceScore());
        response.setRecommendationReason(dto.getRecommendationReason());
        response.setDifficultyLevel(dto.getDifficultyLevel());
        response.setEstimatedHours(dto.getEstimatedHours());
        response.setRelatedCompetencies(dto.getRelatedCompetencies());
        response.setStageId(dto.getStageId());
        response.setStageTitle(dto.getStageTitle());
        response.setDaysUntilDue(dto.getDaysUntilDue());
        return response;
    }
}
