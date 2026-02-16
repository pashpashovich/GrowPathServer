package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.UserRegistrationTokenEntity;

@Repository
public interface UserRegistrationTokenRepository extends JpaRepository<UserRegistrationTokenEntity, Long> {

    Optional<UserRegistrationTokenEntity> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserRegistrationTokenEntity t WHERE t.token = :token")
    void deleteByToken(String token);
}
