package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.CreateRecipientCommand;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateRecipientHandler {

    private final RecipientService recipientService;

    public Recipient handle(CreateRecipientCommand command) {
        return recipientService.createRecipient(command);
    }
}
