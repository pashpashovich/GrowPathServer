package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.DeleteDistributionGroupCommand;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteDistributionGroupHandler {

    private final DistributionGroupService distributionGroupService;

    public void handle(DeleteDistributionGroupCommand command) {
        distributionGroupService.deleteDistributionGroup(command);
    }
}
