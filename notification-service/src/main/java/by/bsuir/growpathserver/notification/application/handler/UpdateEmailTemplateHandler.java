package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.UpdateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateService;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateEmailTemplateHandler {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplate handle(UpdateEmailTemplateCommand command) {
        return emailTemplateService.updateEmailTemplate(command);
    }
}
