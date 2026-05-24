package by.bsuir.growpathserver.notification.application.query;

import lombok.Builder;

@Builder
public record GetMailingHistoryQuery(
        Integer page,
        Integer limit,
        Long mailingId,
        String type
) {
}
