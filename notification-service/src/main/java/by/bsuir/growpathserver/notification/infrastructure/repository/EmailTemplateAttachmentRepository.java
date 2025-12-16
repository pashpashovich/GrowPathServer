package by.bsuir.growpathserver.notification.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateAttachmentEntity;

@Repository
public interface EmailTemplateAttachmentRepository extends JpaRepository<EmailTemplateAttachmentEntity, Long> {
    List<EmailTemplateAttachmentEntity> findByEmailTemplateId(Long emailTemplateId);

    void deleteByEmailTemplateId(Long emailTemplateId);
}
