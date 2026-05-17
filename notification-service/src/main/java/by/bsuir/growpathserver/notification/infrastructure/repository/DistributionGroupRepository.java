package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;

public interface DistributionGroupRepository
        extends JpaRepository<DistributionGroupEntity, Long>, JpaSpecificationExecutor<DistributionGroupEntity> {

    @EntityGraph(attributePaths = "recipients")
    Optional<DistributionGroupEntity> findWithRecipientsById(Long id);

    List<DistributionGroupEntity> findByIdIn(List<Long> ids);
}
