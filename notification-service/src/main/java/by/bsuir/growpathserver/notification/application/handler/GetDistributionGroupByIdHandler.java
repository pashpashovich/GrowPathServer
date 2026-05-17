package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetDistributionGroupByIdQuery;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetDistributionGroupByIdHandler {

    private final DistributionGroupService distributionGroupService;

    public DistributionGroup handle(GetDistributionGroupByIdQuery query) {
        return distributionGroupService.getDistributionGroupById(query);
    }
}
