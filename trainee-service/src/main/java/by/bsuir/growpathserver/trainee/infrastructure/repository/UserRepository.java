package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
