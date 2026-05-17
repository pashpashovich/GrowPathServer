package by.bsuir.growpathserver.notification.application.service;

import java.util.List;

import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.notification.application.command.CreateRecipientCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteRecipientCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateRecipientCommand;
import by.bsuir.growpathserver.notification.application.query.GetRecipientByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetRecipientsQuery;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;

public interface RecipientService {
    Recipient createRecipient(CreateRecipientCommand command);

    Recipient updateRecipient(UpdateRecipientCommand command);

    void deleteRecipient(DeleteRecipientCommand command);

    Recipient getRecipientById(GetRecipientByIdQuery query);

    Page<Recipient> getRecipients(GetRecipientsQuery query);

    List<Recipient> getRecipientsByGroupId(Long groupId);
}
