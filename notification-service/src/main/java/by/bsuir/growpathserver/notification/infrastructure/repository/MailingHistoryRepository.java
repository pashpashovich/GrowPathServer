package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;

public interface MailingHistoryRepository extends JpaRepository<MailingHistoryEntity, Long> {

    boolean existsByMailingIdAndSentAtAfter(Long mailingId, LocalDateTime sentAt);
}
