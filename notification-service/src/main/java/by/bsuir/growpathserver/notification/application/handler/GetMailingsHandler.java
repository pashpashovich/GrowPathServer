package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetMailingsQuery;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMailingsHandler {

    private final MailingService mailingService;

    public Page<Mailing> handle(GetMailingsQuery query) {
        return mailingService.getMailings(query);
    }
}
