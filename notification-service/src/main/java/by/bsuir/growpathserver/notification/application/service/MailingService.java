package by.bsuir.growpathserver.notification.application.service;

import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.notification.application.command.CreateMailingCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteMailingCommand;
import by.bsuir.growpathserver.notification.application.command.SendMailingCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateMailingCommand;
import by.bsuir.growpathserver.notification.application.query.GetMailingByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetMailingsQuery;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;

public interface MailingService {
    Mailing createMailing(CreateMailingCommand command);

    Mailing updateMailing(UpdateMailingCommand command);

    void deleteMailing(DeleteMailingCommand command);

    void sendMailing(SendMailingCommand command);

    Mailing getMailingById(GetMailingByIdQuery query);

    Page<Mailing> getMailings(GetMailingsQuery query);
}
