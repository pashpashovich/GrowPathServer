package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.CreateMailingCommand;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateMailingHandler {

    private final MailingService mailingService;

    public Mailing handle(CreateMailingCommand command) {
        return mailingService.createMailing(command);
    }
}
