package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.domain.entity.UserRegistrationTokenEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRegistrationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationTokenService {

    private final UserRegistrationTokenRepository tokenRepository;

    @Value("${app.registration-token-validity-days:7}")
    private int validityDays;

    @Transactional
    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        UserRegistrationTokenEntity entity = new UserRegistrationTokenEntity();
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setExpiresAt(LocalDateTime.now().plusDays(validityDays));
        tokenRepository.save(entity);
        log.debug("Created registration token for user id={}", userId);
        return token;
    }

    @Transactional
    public Long validateAndConsumeToken(String token) {
        Optional<UserRegistrationTokenEntity> opt = tokenRepository
                .findByTokenAndExpiresAtAfter(token, LocalDateTime.now());
        UserRegistrationTokenEntity entity = opt
                .orElseThrow(() -> new NoSuchElementException("Invalid or expired registration token"));
        tokenRepository.deleteByToken(token);
        return entity.getUserId();
    }
}
