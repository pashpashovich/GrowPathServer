package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetRecipientByIdQuery;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetRecipientByIdHandler {

    private final RecipientService recipientService;

    public Recipient handle(GetRecipientByIdQuery query) {
        return recipientService.getRecipientById(query);
    }
}
