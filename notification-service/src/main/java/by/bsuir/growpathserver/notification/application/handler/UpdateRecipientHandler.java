package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.UpdateRecipientCommand;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipientHandler {

    private final RecipientService recipientService;

    public Recipient handle(UpdateRecipientCommand command) {
        return recipientService.updateRecipient(command);
    }
}
