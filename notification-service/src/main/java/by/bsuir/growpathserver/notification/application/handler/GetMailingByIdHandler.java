package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetMailingByIdQuery;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMailingByIdHandler {

    private final MailingService mailingService;

    public Mailing handle(GetMailingByIdQuery query) {
        return mailingService.getMailingById(query);
    }
}
