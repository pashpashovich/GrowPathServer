package by.bsuir.growpathserver.notification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateEntity;

@Repository
public interface EmailTemplateRepository
        extends JpaRepository<EmailTemplateEntity, Long>, JpaSpecificationExecutor<EmailTemplateEntity> {
}
