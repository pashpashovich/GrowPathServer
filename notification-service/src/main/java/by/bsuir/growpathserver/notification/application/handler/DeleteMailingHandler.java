package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.DeleteMailingCommand;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteMailingHandler {

    private final MailingService mailingService;

    public void handle(DeleteMailingCommand command) {
        mailingService.deleteMailing(command);
    }
}
