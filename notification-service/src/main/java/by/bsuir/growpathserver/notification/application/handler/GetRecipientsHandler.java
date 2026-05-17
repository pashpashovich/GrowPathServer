package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetRecipientsQuery;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetRecipientsHandler {

    private final RecipientService recipientService;

    public Page<Recipient> handle(GetRecipientsQuery query) {
        return recipientService.getRecipients(query);
    }
}
