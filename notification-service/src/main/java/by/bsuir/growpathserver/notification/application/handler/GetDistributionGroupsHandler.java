package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupsQuery;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetDistributionGroupsHandler {

    private final DistributionGroupService distributionGroupService;

    public Page<DistributionGroup> handle(GetDistributionGroupsQuery query) {
        return distributionGroupService.getDistributionGroups(query);
    }
}
