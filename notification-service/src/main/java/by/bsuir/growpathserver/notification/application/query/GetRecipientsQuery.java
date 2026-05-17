package by.bsuir.growpathserver.notification.application.query;

import lombok.Builder;

@Builder
public record GetRecipientsQuery(Integer page, Integer limit, String type) {
}
