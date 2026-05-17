package by.bsuir.growpathserver.notification.application.command;

import lombok.Builder;

@Builder
public record UpdateRecipientCommand(
        Long id,
        String email,
        String fullName,
        Long userId
) {
}
