package by.bsuir.growpathserver.notification.application.command;

import java.util.List;

import lombok.Builder;

@Builder
public record CreateEmailTemplateCommand(
        String name,
        String subject,
        String body,
        List<Attachment> attachments
) {
    public record Attachment(String name, String token) {
    }
}
