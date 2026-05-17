package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.UpdateDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateDistributionGroupHandler {

    private final DistributionGroupService distributionGroupService;

    public DistributionGroup handle(UpdateDistributionGroupCommand command) {
        return distributionGroupService.updateDistributionGroup(command);
    }
}
