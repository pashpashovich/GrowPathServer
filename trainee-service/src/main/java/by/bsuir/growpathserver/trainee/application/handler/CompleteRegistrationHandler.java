package by.bsuir.growpathserver.trainee.application.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CompleteRegistrationCommand;
import by.bsuir.growpathserver.trainee.application.port.IdentityProviderPort;
import by.bsuir.growpathserver.trainee.application.service.RegistrationTokenService;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompleteRegistrationHandler {

    private final RegistrationTokenService registrationTokenService;
    private final UserRepository userRepository;
    private final IdentityProviderPort identityProviderPort;

    @Transactional
    public void handle(CompleteRegistrationCommand command) {
        String token = Objects.nonNull(command.token()) ? command.token().trim() : "";
        if (StringUtils.isNoneEmpty(token)) {
            Long userId = registrationTokenService.validateAndConsumeToken(token);
            String email = userRepository.findById(userId)
                    .orElseThrow()
                    .getEmail();

            identityProviderPort.setPassword(email, command.newPassword());
        }
    }
}
