package by.bsuir.growpathserver.notification.application.command;

import lombok.Builder;

@Builder
public record UpdateDistributionGroupCommand(Long id, String name, String description) {
}
