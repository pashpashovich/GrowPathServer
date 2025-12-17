package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetEmailTemplatesQuery;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateService;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetEmailTemplatesHandler {

    private final EmailTemplateService emailTemplateService;

    public Page<EmailTemplate> handle(GetEmailTemplatesQuery query) {
        return emailTemplateService.getEmailTemplates(query);
    }
}
