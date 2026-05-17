package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.DeleteRecipientCommand;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteRecipientHandler {

    private final RecipientService recipientService;

    public void handle(DeleteRecipientCommand command) {
        recipientService.deleteRecipient(command);
    }
}
