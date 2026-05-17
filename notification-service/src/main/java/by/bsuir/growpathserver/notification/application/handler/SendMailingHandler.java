package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.SendMailingCommand;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SendMailingHandler {

    private final MailingService mailingService;

    public void handle(SendMailingCommand command) {
        mailingService.sendMailing(command);
    }
}
