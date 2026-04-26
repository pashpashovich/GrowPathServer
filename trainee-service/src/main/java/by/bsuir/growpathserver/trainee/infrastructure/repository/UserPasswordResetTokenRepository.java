package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.UserPasswordResetTokenEntity;

@Repository
public interface UserPasswordResetTokenRepository extends JpaRepository<UserPasswordResetTokenEntity, Long> {

    Optional<UserPasswordResetTokenEntity> findByTokenAndExpiresAtAfter(String token, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserPasswordResetTokenEntity t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserPasswordResetTokenEntity t WHERE t.token = :token")
    void deleteByToken(@Param("token") String token);
}
