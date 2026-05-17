package by.bsuir.growpathserver.notification.application.command;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import by.bsuir.growpathserver.notification.domain.valueobject.WeekDay;
import lombok.Builder;

@Builder
public record CreateMailingCommand(
        String name,
        String type,
        Long emailTemplateId,
        LocalDateTime executeAt,
        List<Long> distributionGroupIds,
        WeekDay weekDay,
        LocalTime executeTime
) {
}
