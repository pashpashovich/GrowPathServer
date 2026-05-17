package by.bsuir.growpathserver.notification.domain.aggregate;

import java.time.LocalDateTime;
import java.util.List;

import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import lombok.Getter;

@Getter
public class Mailing {
    private final Long id;
    private final String name;
    private final MailingType type;
    private final MailingStatus status;
    private final Long emailTemplateId;
    private final LocalDateTime executeAt;
    private final List<Long> distributionGroupIds;
    private final List<MailingScheduleDefinition> scheduleDefinitions;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Mailing(Long id,
                    String name,
                    MailingType type,
                    MailingStatus status,
                    Long emailTemplateId,
                    LocalDateTime executeAt,
                    List<Long> distributionGroupIds,
                    List<MailingScheduleDefinition> scheduleDefinitions,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.emailTemplateId = emailTemplateId;
        this.executeAt = executeAt;
        this.distributionGroupIds = distributionGroupIds;
        this.scheduleDefinitions = scheduleDefinitions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Mailing fromEntity(MailingEntity entity) {
        List<Long> groupIds = entity.getDistributionGroups() == null
                ? List.of()
                : entity.getDistributionGroups().stream()
                .map(g -> g.getId())
                .toList();
        List<MailingScheduleDefinition> schedules = entity.getSchedules() == null
                ? List.of()
                : entity.getSchedules().stream()
                .map(s -> new MailingScheduleDefinition(s.getWeekDay(), s.getExecuteTime()))
                .toList();
        return new Mailing(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getStatus(),
                entity.getTemplate().getId(),
                entity.getExecuteAt(),
                groupIds,
                schedules,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static Mailing create(String name,
                                 MailingType type,
                                 Long emailTemplateId,
                                 LocalDateTime executeAt,
                                 List<Long> distributionGroupIds,
                                 MailingScheduleDefinition schedule) {
        validateCreate(name, type, emailTemplateId, executeAt, distributionGroupIds, schedule);

        MailingStatus initialStatus = switch (type) {
            case IMMEDIATE -> MailingStatus.DRAFT;
            case SCHEDULED, RECURRING -> MailingStatus.SCHEDULED;
        };

        List<MailingScheduleDefinition> schedules = type == MailingType.RECURRING && schedule != null
                ? List.of(schedule)
                : List.of();

        return new Mailing(
                null,
                name.trim(),
                type,
                initialStatus,
                emailTemplateId,
                executeAt,
                distributionGroupIds,
                schedules,
                null,
                null
        );
    }

    private static void validateCreate(String name,
                                       MailingType type,
                                       Long emailTemplateId,
                                       LocalDateTime executeAt,
                                       List<Long> distributionGroupIds,
                                       MailingScheduleDefinition schedule) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mailing name is required");
        }
        if (emailTemplateId == null) {
            throw new IllegalArgumentException("Email template id is required");
        }
        if (distributionGroupIds == null || distributionGroupIds.isEmpty()) {
            throw new IllegalArgumentException("At least one distribution group is required");
        }
        if (type == MailingType.SCHEDULED) {
            if (executeAt == null) {
                throw new IllegalArgumentException("executeAt is required for scheduled mailings");
            }
            if (!executeAt.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("executeAt must be in the future");
            }
        }
        if (type == MailingType.RECURRING) {
            if (schedule == null || schedule.weekDay() == null || schedule.executeTime() == null) {
                throw new IllegalArgumentException("schedule is required for recurring mailings");
            }
        }
    }

    public boolean canSend() {
        return status == MailingStatus.DRAFT || status == MailingStatus.SCHEDULED;
    }
}
