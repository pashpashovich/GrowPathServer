package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import by.bsuir.growpathserver.notification.domain.entity.RecipientEntity;

public interface RecipientRepository
        extends JpaRepository<RecipientEntity, Long>, JpaSpecificationExecutor<RecipientEntity> {

    @EntityGraph(attributePaths = "distributionGroups")
    Optional<RecipientEntity> findWithGroupsById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
