package by.bsuir.growpathserver.notification.application.query;

import lombok.Builder;

@Builder
public record GetEmailTemplatesQuery(
        Integer page,
        Integer limit
) {
}
