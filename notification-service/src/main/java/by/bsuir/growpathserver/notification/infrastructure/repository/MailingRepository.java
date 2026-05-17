package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;

public interface MailingRepository extends JpaRepository<MailingEntity, Long>, JpaSpecificationExecutor<MailingEntity> {

    @EntityGraph(attributePaths = { "template", "distributionGroups", "schedules" })
    Optional<MailingEntity> findWithDetailsById(Long id);

    @Query("""
            SELECT m FROM MailingEntity m
            WHERE m.status = :status
              AND m.type = :type
              AND m.executeAt <= :now
            """)
    List<MailingEntity> findDueScheduledMailings(@Param("status") MailingStatus status,
                                                 @Param("type") MailingType type,
                                                 @Param("now") LocalDateTime now);

    @Query("""
            SELECT m FROM MailingEntity m
            WHERE m.status = :status
              AND m.type = :type
            """)
    List<MailingEntity> findActiveRecurringMailings(@Param("status") MailingStatus status,
                                                    @Param("type") MailingType type);

    boolean existsByTemplateIdAndStatus(Long templateId, MailingStatus status);
}
