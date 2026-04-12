package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LastLoginService {

    private final UserRepository userRepository;

    @Value("${growpath.security.last-login-interval-minutes:5}")
    private int minIntervalMinutes;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIfStale(String keycloakSub, String loginEmail) {
        try {
            Optional<UserEntity> user = Optional.empty();
            if (StringUtils.isNotBlank(keycloakSub)) {
                user = userRepository.findByKeycloakUserId(keycloakSub);
            }
            if (user.isEmpty() && StringUtils.isNotBlank(loginEmail)) {
                user = userRepository.findByEmail(loginEmail);
            }
            if (user.isEmpty()) {
                return;
            }
            UserEntity entity = user.get();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last = entity.getLastLogin();
            boolean touchLogin = last == null || last.isBefore(now.minusMinutes(minIntervalMinutes));
            boolean linkKeycloak = StringUtils.isNotBlank(keycloakSub)
                    && StringUtils.isBlank(entity.getKeycloakUserId());
            if (touchLogin) {
                entity.setLastLogin(now);
            }
            if (linkKeycloak) {
                entity.setKeycloakUserId(keycloakSub);
            }
            if (touchLogin || linkKeycloak) {
                userRepository.save(entity);
            }
        }
        catch (Exception e) {
            log.warn("Could not update last_login: {}", e.getMessage());
        }
    }
}
