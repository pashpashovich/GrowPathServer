package by.bsuir.growpathserver.notification.application.command;

import lombok.Builder;

@Builder
public record CreateRecipientCommand(
        String email,
        String fullName,
        Long userId,
        String type
) {
}
