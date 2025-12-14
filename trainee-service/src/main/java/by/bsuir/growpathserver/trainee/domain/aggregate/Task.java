package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import lombok.Getter;

@Getter
public class Task {
    private final String id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final String assigneeId;
    private final String mentorId;
    private final String internshipId;
    private final String stageId;
    private final LocalDateTime dueDate;
    private final LocalDateTime takenAt;
    private final LocalDateTime submittedAt;
    private final LocalDateTime completedAt;
    private final Double rating;
    private final String reviewComment;
    private final String submissionComment;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Task(TaskEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.status = entity.getStatus();
        this.priority = entity.getPriority();
        this.assigneeId = entity.getAssigneeId();
        this.mentorId = entity.getMentorId();
        this.internshipId = entity.getInternshipId();
        this.stageId = entity.getStageId();
        this.dueDate = entity.getDueDate();
        this.takenAt = entity.getTakenAt();
        this.submittedAt = entity.getSubmittedAt();
        this.completedAt = entity.getCompletedAt();
        this.rating = entity.getRating();
        this.reviewComment = entity.getReviewComment();
        this.submissionComment = entity.getSubmissionComment();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public static Task fromEntity(TaskEntity entity) {
        return new Task(entity);
    }

    public TaskEntity toEntity() {
        TaskEntity entity = new TaskEntity();
        entity.setId(this.id);
        entity.setTitle(this.title);
        entity.setDescription(this.description);
        entity.setStatus(this.status);
        entity.setPriority(this.priority);
        entity.setAssigneeId(this.assigneeId);
        entity.setMentorId(this.mentorId);
        entity.setInternshipId(this.internshipId);
        entity.setStageId(this.stageId);
        entity.setDueDate(this.dueDate);
        entity.setTakenAt(this.takenAt);
        entity.setSubmittedAt(this.submittedAt);
        entity.setCompletedAt(this.completedAt);
        entity.setRating(this.rating);
        entity.setReviewComment(this.reviewComment);
        entity.setSubmissionComment(this.submissionComment);
        entity.setCreatedAt(this.createdAt);
        entity.setUpdatedAt(this.updatedAt);
        return entity;
    }

    public static Task create(String title, String description, TaskPriority priority,
                              String mentorId, String internshipId, String stageId,
                              String assigneeId, LocalDateTime dueDate) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setPriority(priority);
        entity.setMentorId(mentorId);
        entity.setInternshipId(internshipId);
        entity.setStageId(stageId);
        entity.setAssigneeId(assigneeId);
        entity.setDueDate(dueDate);
        return new Task(entity);
    }
}
