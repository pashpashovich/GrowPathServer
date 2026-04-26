package by.bsuir.growpathserver.trainee.application.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.ResetPasswordCommand;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.application.service.PasswordResetTokenService;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResetPasswordHandler {

    private final PasswordResetTokenService passwordResetTokenService;
    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;

    public void handle(ResetPasswordCommand command) {
        String token = Objects.nonNull(command.token()) ? command.token().trim() : "";
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Token is required");
        }
        Long userId = passwordResetTokenService.requireValidUserIdForResetToken(token);
        UserEntity user = userRepository.findById(userId).orElseThrow();
        identityProviderPort.setPassword(user.getEmail(), command.newPassword());
        passwordResetTokenService.deleteResetToken(token);
    }
}
