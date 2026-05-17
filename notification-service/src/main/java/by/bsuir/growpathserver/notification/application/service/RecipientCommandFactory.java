package by.bsuir.growpathserver.notification.application.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import by.bsuir.growpathserver.dto.model.CreateRecipientRequest;
import by.bsuir.growpathserver.notification.application.command.CreateRecipientCommand;
import by.bsuir.growpathserver.notification.application.exception.ExternalRecipientDataRequiredException;
import by.bsuir.growpathserver.notification.application.exception.ExternalRecipientUserIdNotAllowedException;
import by.bsuir.growpathserver.notification.application.exception.InternalRecipientUserIdRequiredException;
import by.bsuir.growpathserver.notification.application.exception.TraineeUserNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.UnknownEnumerationValueException;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.domain.valueobject.RecipientType;
import by.bsuir.growpathserver.notification.infrastructure.client.TraineeUserClient;
import by.bsuir.growpathserver.notification.infrastructure.client.dto.TraineeUserDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecipientCommandFactory {

    private final TraineeUserClient traineeUserClient;

    public CreateRecipientCommand buildCreateCommand(CreateRecipientRequest request) {
        RecipientType type = parseRecipientType(request.getType().getValue());
        if (RecipientType.USER.equals(type)) {
            return buildInternalUserCommand(request);
        }
        return buildExternalRecipientCommand(request);
    }

    private CreateRecipientCommand buildInternalUserCommand(CreateRecipientRequest request) {
        if (!StringUtils.hasText(request.getUserId())) {
            throw new InternalRecipientUserIdRequiredException();
        }
        Long userId = NotificationIds.parseRequired(request.getUserId(), "userId");
        TraineeUserDto user = traineeUserClient.getUserById(userId);
        if (user == null || user.getId() == null) {
            throw new TraineeUserNotFoundException(userId);
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new TraineeUserNotFoundException(userId);
        }
        return CreateRecipientCommand.builder()
                .email(user.getEmail().trim())
                .fullName(formatFullName(user))
                .userId(userId)
                .type(RecipientType.USER.toApiValue())
                .build();
    }

    private CreateRecipientCommand buildExternalRecipientCommand(CreateRecipientRequest request) {
        if (StringUtils.hasText(request.getUserId())) {
            throw new ExternalRecipientUserIdNotAllowedException();
        }
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getFullName())) {
            throw new ExternalRecipientDataRequiredException();
        }
        return CreateRecipientCommand.builder()
                .email(request.getEmail().trim())
                .fullName(request.getFullName().trim())
                .userId(null)
                .type(RecipientType.EXTERNAL.toApiValue())
                .build();
    }

    private String formatFullName(TraineeUserDto user) {
        StringBuilder fullName = new StringBuilder();
        if (StringUtils.hasText(user.getLastName())) {
            fullName.append(user.getLastName().trim());
        }
        if (StringUtils.hasText(user.getFirstName())) {
            if (!fullName.isEmpty()) {
                fullName.append(' ');
            }
            fullName.append(user.getFirstName().trim());
        }
        if (StringUtils.hasText(user.getPatronymicName())) {
            if (!fullName.isEmpty()) {
                fullName.append(' ');
            }
            fullName.append(user.getPatronymicName().trim());
        }
        return fullName.toString();
    }

    private RecipientType parseRecipientType(String value) {
        try {
            return RecipientType.fromApiValue(value);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("recipient type", value);
        }
    }
}
