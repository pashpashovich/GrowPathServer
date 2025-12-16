package by.bsuir.growpathserver.notification.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.query.GetEmailTemplateByIdQuery;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateService;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetEmailTemplateByIdHandler {

    private final EmailTemplateService emailTemplateService;

    public EmailTemplate handle(GetEmailTemplateByIdQuery query) {
        return emailTemplateService.getEmailTemplateById(query);
    }
}
