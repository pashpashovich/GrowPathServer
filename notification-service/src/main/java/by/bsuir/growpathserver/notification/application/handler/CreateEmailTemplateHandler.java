package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.CreateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateService;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateEmailTemplateHandler {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplate handle(CreateEmailTemplateCommand command) {
        return emailTemplateService.createEmailTemplate(command);
    }
}
