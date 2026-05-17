package by.bsuir.growpathserver.notification.application.query;

import lombok.Builder;

@Builder
public record GetDistributionGroupsQuery(Integer page, Integer limit) {
}
