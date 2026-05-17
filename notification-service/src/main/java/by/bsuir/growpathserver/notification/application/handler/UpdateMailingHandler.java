package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.UpdateMailingCommand;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateMailingHandler {

    private final MailingService mailingService;

    public Mailing handle(UpdateMailingCommand command) {
        return mailingService.updateMailing(command);
    }
}
