package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.CreateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateDistributionGroupHandler {

    private final DistributionGroupService distributionGroupService;

    public DistributionGroup handle(CreateDistributionGroupCommand command) {
        return distributionGroupService.createDistributionGroup(command);
    }
}
