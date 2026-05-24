package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;

public interface MailingHistoryRepository extends JpaRepository<MailingHistoryEntity, Long>,
        JpaSpecificationExecutor<MailingHistoryEntity> {

    boolean existsByMailingIdAndSentAtAfter(Long mailingId, LocalDateTime sentAt);
}
