package by.bsuir.growpathserver.notification.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.exception.DistributionGroupNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.MailingDispatchFailedException;
import by.bsuir.growpathserver.notification.application.exception.MailingNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.MailingSendNotAllowedException;
import by.bsuir.growpathserver.notification.application.service.BulkEmailService.EmailAttachment;
import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateAttachmentEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;
import by.bsuir.growpathserver.notification.domain.entity.RecipientEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import by.bsuir.growpathserver.notification.infrastructure.repository.DistributionGroupRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingHistoryRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingRepository;
import by.bsuir.growpathserver.notification.infrastructure.util.AttachmentContentTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingDispatchService {

    private final MailingRepository mailingRepository;
    private final DistributionGroupRepository distributionGroupRepository;
    private final MailingHistoryRepository mailingHistoryRepository;
    private final EmailTemplateAttachmentRepository emailTemplateAttachmentRepository;
    private final EmailTemplateAttachmentStorageService attachmentStorageService;
    private final BulkEmailService bulkEmailService;

    @Transactional
    public void dispatch(Long mailingId) {
        MailingEntity mailing = mailingRepository.findWithDetailsById(mailingId)
                .orElseThrow(() -> new MailingNotFoundException(mailingId));

        if (mailing.getStatus() == MailingStatus.CANCELLED || mailing.getStatus() == MailingStatus.SENT) {
            throw new MailingSendNotAllowedException(mailing.getStatus());
        }

        var template = mailing.getTemplate();
        List<EmailAttachment> emailAttachments = loadTemplateAttachments(template.getId());
        Set<RecipientEntity> uniqueRecipients = new HashSet<>();
        for (DistributionGroupEntity groupRef : mailing.getDistributionGroups()) {
            DistributionGroupEntity group = distributionGroupRepository.findWithRecipientsById(groupRef.getId())
                    .orElseThrow(() -> new DistributionGroupNotFoundException(groupRef.getId()));
            uniqueRecipients.addAll(group.getRecipients());
        }

        int sentCount = 0;
        for (RecipientEntity recipient : uniqueRecipients) {
            try {
                bulkEmailService.sendHtmlEmail(
                        recipient.getEmail(),
                        recipient.getFullName(),
                        template.getSubject(),
                        template.getBody(),
                        emailAttachments
                );
                sentCount++;
            }
            catch (Exception e) {
                log.warn("Skipped recipient {} for mailing {}: {}", recipient.getEmail(), mailingId, e.getMessage());
            }
        }

        if (sentCount == 0) {
            throw new MailingDispatchFailedException(mailingId);
        }

        MailingHistoryEntity history = new MailingHistoryEntity();
        history.setMailingId(mailing.getId());
        history.setName(mailing.getName());
        history.setTemplateName(template.getName());
        history.setMailingType(mailing.getType());
        history.setSentAt(LocalDateTime.now());
        mailingHistoryRepository.save(history);

        if (mailing.getType() == MailingType.SCHEDULED || mailing.getType() == MailingType.IMMEDIATE) {
            mailing.setStatus(MailingStatus.SENT);
        }
        mailingRepository.save(mailing);

        log.info("Mailing {} dispatched to {} recipients", mailingId, sentCount);
    }

    private List<EmailAttachment> loadTemplateAttachments(Long templateId) {
        List<EmailTemplateAttachmentEntity> attachments = emailTemplateAttachmentRepository.findByEmailTemplateId(
                templateId);
        if (attachments.isEmpty()) {
            return List.of();
        }
        List<EmailAttachment> result = new ArrayList<>(attachments.size());
        for (EmailTemplateAttachmentEntity attachment : attachments) {
            byte[] content = attachmentStorageService.downloadBytes(attachment.getToken());
            result.add(new EmailAttachment(
                    attachment.getName(),
                    content,
                    AttachmentContentTypeResolver.resolve(attachment.getName())
            ));
        }
        return result;
    }
}
