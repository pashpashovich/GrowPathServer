package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;

public interface DistributionGroupRepository
        extends JpaRepository<DistributionGroupEntity, Long>, JpaSpecificationExecutor<DistributionGroupEntity> {

    @EntityGraph(attributePaths = "recipients")
    Optional<DistributionGroupEntity> findWithRecipientsById(Long id);

    List<DistributionGroupEntity> findByIdIn(List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM recipient_distribution_group WHERE distribution_group_id = :groupId", nativeQuery = true)
    void deleteRecipientMemberships(@Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM mailing_recipient_group WHERE distribution_group_id = :groupId", nativeQuery = true)
    void deleteMailingMemberships(@Param("groupId") Long groupId);
}
