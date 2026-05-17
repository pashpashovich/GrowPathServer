package by.bsuir.growpathserver.notification.application.command;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record UpdateMailingCommand(
        Long id,
        String name,
        String type,
        Long emailTemplateId,
        LocalDateTime executeAt,
        List<Long> distributionGroupIds
) {
}
