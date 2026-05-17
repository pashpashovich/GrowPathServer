package by.bsuir.growpathserver.notification.application.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.CreateRecipientRequest;
import by.bsuir.growpathserver.dto.model.RecipientListResponse;
import by.bsuir.growpathserver.dto.model.RecipientResponse;
import by.bsuir.growpathserver.dto.model.UpdateRecipientRequest;
import by.bsuir.growpathserver.notification.application.command.DeleteRecipientCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateRecipientCommand;
import by.bsuir.growpathserver.notification.application.handler.CreateRecipientHandler;
import by.bsuir.growpathserver.notification.application.handler.DeleteRecipientHandler;
import by.bsuir.growpathserver.notification.application.handler.GetRecipientByIdHandler;
import by.bsuir.growpathserver.notification.application.handler.GetRecipientsHandler;
import by.bsuir.growpathserver.notification.application.handler.UpdateRecipientHandler;
import by.bsuir.growpathserver.notification.application.query.GetRecipientByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetRecipientsQuery;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import by.bsuir.growpathserver.notification.infrastructure.mapper.RecipientMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientApplicationFacade {

    private final CreateRecipientHandler createRecipientHandler;
    private final GetRecipientByIdHandler getRecipientByIdHandler;
    private final GetRecipientsHandler getRecipientsHandler;
    private final UpdateRecipientHandler updateRecipientHandler;
    private final DeleteRecipientHandler deleteRecipientHandler;
    private final RecipientCommandFactory recipientCommandFactory;
    private final RecipientMapper recipientMapper;

    public RecipientResponse createRecipient(CreateRecipientRequest request) {
        Recipient recipient = createRecipientHandler.handle(recipientCommandFactory.buildCreateCommand(request));
        return recipientMapper.toRecipientResponse(recipient);
    }

    public void deleteRecipient(String id) {
        deleteRecipientHandler.handle(new DeleteRecipientCommand(NotificationIds.parseRequired(id, "id")));
    }

    public RecipientResponse getRecipientById(String id) {
        Recipient recipient = getRecipientByIdHandler.handle(
                new GetRecipientByIdQuery(NotificationIds.parseRequired(id, "id")));
        return recipientMapper.toRecipientResponse(recipient);
    }

    public RecipientListResponse getRecipients(Integer page, Integer limit, String type) {
        Page<Recipient> recipients = getRecipientsHandler.handle(GetRecipientsQuery.builder()
                                                                         .page(page)
                                                                         .limit(limit)
                                                                         .type(type)
                                                                         .build());
        return recipientMapper.toRecipientListResponse(recipients);
    }

    public RecipientResponse updateRecipient(String id, UpdateRecipientRequest request) {
        Recipient recipient = updateRecipientHandler.handle(UpdateRecipientCommand.builder()
                                                                    .id(NotificationIds.parseRequired(id, "id"))
                                                                    .email(request.getEmail())
                                                                    .fullName(request.getFullName())
                                                                    .userId(NotificationIds.parseOptional(
                                                                            request.getUserId(), "userId"))
                                                                    .build());
        return recipientMapper.toRecipientResponse(recipient);
    }
}
