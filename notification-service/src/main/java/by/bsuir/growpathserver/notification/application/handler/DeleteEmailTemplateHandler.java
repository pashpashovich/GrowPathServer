package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.command.DeleteEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteEmailTemplateHandler {

    private final EmailTemplateService emailTemplateService;

    public void handle(DeleteEmailTemplateCommand command) {
        emailTemplateService.deleteEmailTemplate(command);
    }
}
