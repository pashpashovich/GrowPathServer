package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.AddRecipientToGroupCommand;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AddRecipientToGroupHandler {

    private final DistributionGroupService distributionGroupService;

    public void handle(AddRecipientToGroupCommand command) {
        distributionGroupService.addRecipientToGroup(command);
    }
}
