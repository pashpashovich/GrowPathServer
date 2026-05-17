package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.RemoveRecipientFromGroupCommand;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveRecipientFromGroupHandler {

    private final DistributionGroupService distributionGroupService;

    public void handle(RemoveRecipientFromGroupCommand command) {
        distributionGroupService.removeRecipientFromGroup(command);
    }
}
