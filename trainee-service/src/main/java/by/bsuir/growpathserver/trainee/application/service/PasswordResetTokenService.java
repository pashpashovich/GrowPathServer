package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.exception.InvalidPasswordResetTokenException;
import by.bsuir.growpathserver.trainee.domain.entity.UserPasswordResetTokenEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final UserPasswordResetTokenRepository tokenRepository;

    @Value("${app.password-reset-token-validity-hours:24}")
    private int validityHours;

    @Transactional
    public String createToken(Long userId) {
        tokenRepository.deleteByUserId(userId);
        String token = UUID.randomUUID().toString().replace("-", "");
        UserPasswordResetTokenEntity entity = new UserPasswordResetTokenEntity();
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setExpiresAt(LocalDateTime.now().plusHours(validityHours));
        tokenRepository.save(entity);
        log.info("Created password reset token for user id={}", userId);
        return token;
    }

    @Transactional(readOnly = true)
    public Long requireValidUserIdForResetToken(String token) {
        Optional<UserPasswordResetTokenEntity> opt = tokenRepository
                .findByTokenAndExpiresAtAfter(token, LocalDateTime.now());
        if (opt.isEmpty()) {
            log.warn("Invalid or expired password reset token");
            throw new InvalidPasswordResetTokenException();
        }
        return opt.get().getUserId();
    }

    @Transactional
    public void deleteResetToken(String token) {
        tokenRepository.deleteByToken(token);
    }
}
