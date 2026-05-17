package by.bsuir.growpathserver.notification.application.command;

import lombok.Builder;

@Builder
public record CreateDistributionGroupCommand(String name, String description) {
}
