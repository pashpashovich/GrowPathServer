package by.bsuir.growpathserver.notification.application.query;

import lombok.Builder;

@Builder
public record GetMailingsQuery(
        Integer page,
        Integer limit,
        String status,
        String type
) {
}
