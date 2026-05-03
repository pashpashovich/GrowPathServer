package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByKeycloakUserId(String keycloakUserId);

    @Query("SELECT u FROM UserEntity u WHERE u.role = :role")
    List<UserEntity> findByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM UserEntity u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<UserEntity> findActiveByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.role = :role")
    Long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.role = :role AND u.status = 'ACTIVE'")
    Long countActiveByRole(@Param("role") UserRole role);
}
