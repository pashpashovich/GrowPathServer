package by.bsuir.growpathserver.trainee.domain.aggregate;

import java.time.LocalDateTime;

import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import lombok.Getter;

@Getter
public class Assessment {
    private final Long id;
    private final Long internId;
    private final Long mentorId;
    private final Long internshipId;
    private final Double overallRating;
    private final Double qualityRating;
    private final Double speedRating;
    private final Double communicationRating;
    private final String comment;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Assessment(AssessmentEntity entity) {
        this.id = entity.getId();
        this.internId = entity.getInternId();
        this.mentorId = entity.getMentorId();
        this.internshipId = entity.getInternshipId();
        this.overallRating = entity.getOverallRating();
        this.qualityRating = entity.getQualityRating();
        this.speedRating = entity.getSpeedRating();
        this.communicationRating = entity.getCommunicationRating();
        this.comment = entity.getComment();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    public static Assessment fromEntity(AssessmentEntity entity) {
        return new Assessment(entity);
    }

    public AssessmentEntity toEntity() {
        AssessmentEntity entity = new AssessmentEntity();
        entity.setId(this.id);
        entity.setInternId(this.internId);
        entity.setMentorId(this.mentorId);
        entity.setInternshipId(this.internshipId);
        entity.setOverallRating(this.overallRating);
        entity.setQualityRating(this.qualityRating);
        entity.setSpeedRating(this.speedRating);
        entity.setCommunicationRating(this.communicationRating);
        entity.setComment(this.comment);
        entity.setCreatedAt(this.createdAt);
        entity.setUpdatedAt(this.updatedAt);
        return entity;
    }

    public static Assessment create(Long internId, Long mentorId, Long internshipId,
                                    Double overallRating, Double qualityRating,
                                    Double speedRating, Double communicationRating, String comment) {
        AssessmentEntity entity = new AssessmentEntity();
        entity.setInternId(internId);
        entity.setMentorId(mentorId);
        entity.setInternshipId(internshipId);
        entity.setOverallRating(overallRating);
        entity.setQualityRating(qualityRating);
        entity.setSpeedRating(speedRating);
        entity.setCommunicationRating(communicationRating);
        entity.setComment(comment);
        return new Assessment(entity);
    }
}
