package by.bsuir.growpathserver.notification.application.command;

import java.util.List;

import lombok.Builder;

@Builder
public record UpdateEmailTemplateCommand(
        Long id,
        String name,
        String subject,
        String body,
        List<Attachment> attachments
) {
    public record Attachment(
            String name,
            String token
    ) {
    }
}
