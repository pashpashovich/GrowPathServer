package by.bsuir.growpathserver.notification.application.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.command.CreateMailingCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteMailingCommand;
import by.bsuir.growpathserver.notification.application.command.SendMailingCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateMailingCommand;
import by.bsuir.growpathserver.notification.application.exception.DistributionGroupsNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.EmailTemplateNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.InvalidMailingConfigurationException;
import by.bsuir.growpathserver.notification.application.exception.MailingAlreadySentException;
import by.bsuir.growpathserver.notification.application.exception.MailingNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.MailingSendNotAllowedException;
import by.bsuir.growpathserver.notification.application.exception.UnknownEnumerationValueException;
import by.bsuir.growpathserver.notification.application.query.GetMailingByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetMailingsQuery;
import by.bsuir.growpathserver.notification.application.service.MailingDispatchService;
import by.bsuir.growpathserver.notification.application.service.MailingService;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import by.bsuir.growpathserver.notification.domain.aggregate.MailingScheduleDefinition;
import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import by.bsuir.growpathserver.notification.infrastructure.persistence.MailingEntityFactory;
import by.bsuir.growpathserver.notification.infrastructure.repository.DistributionGroupRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailingServiceImpl implements MailingService {

    private final MailingRepository mailingRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final DistributionGroupRepository distributionGroupRepository;
    private final MailingEntityFactory mailingEntityFactory;
    private final MailingDispatchService mailingDispatchService;

    @Override
    @Transactional
    public Mailing createMailing(CreateMailingCommand command) {
        MailingType type = parseMailingType(command.type());
        MailingScheduleDefinition schedule = command.weekDay() != null && command.executeTime() != null
                ? new MailingScheduleDefinition(command.weekDay(), command.executeTime())
                : null;

        Mailing mailing = createMailingAggregate(
                command.name(),
                type,
                command.emailTemplateId(),
                command.executeAt(),
                command.distributionGroupIds(),
                schedule
        );

        EmailTemplateEntity template = requireTemplate(command.emailTemplateId());
        List<DistributionGroupEntity> groups = requireGroups(command.distributionGroupIds());

        MailingEntity saved = mailingRepository.save(mailingEntityFactory.toNewEntity(mailing, template, groups));

        if (type == MailingType.IMMEDIATE) {
            mailingDispatchService.dispatch(saved.getId());
            saved = mailingRepository.findWithDetailsById(saved.getId()).orElse(saved);
        }

        return Mailing.fromEntity(saved);
    }

    @Override
    @Transactional
    public Mailing updateMailing(UpdateMailingCommand command) {
        MailingEntity entity = requireMailing(command.id());

        if (entity.getStatus() == MailingStatus.SENT) {
            throw new MailingAlreadySentException();
        }

        if (command.name() != null) {
            entity.setName(command.name());
        }
        if (command.type() != null) {
            entity.setType(parseMailingType(command.type()));
        }
        if (command.emailTemplateId() != null) {
            entity.setTemplate(requireTemplate(command.emailTemplateId()));
        }
        if (command.executeAt() != null) {
            entity.setExecuteAt(command.executeAt());
        }
        if (command.distributionGroupIds() != null && !command.distributionGroupIds().isEmpty()) {
            entity.setDistributionGroups(requireGroups(command.distributionGroupIds()));
        }

        MailingEntity saved = mailingRepository.save(entity);
        return Mailing.fromEntity(mailingRepository.findWithDetailsById(saved.getId()).orElse(saved));
    }

    @Override
    @Transactional
    public void deleteMailing(DeleteMailingCommand command) {
        MailingEntity entity = requireMailing(command.id());
        if (entity.getStatus() == MailingStatus.SCHEDULED) {
            entity.setStatus(MailingStatus.CANCELLED);
            mailingRepository.save(entity);
        }
        mailingRepository.delete(entity);
    }

    @Override
    @Transactional
    public void sendMailing(SendMailingCommand command) {
        MailingEntity entity = requireMailing(command.id());
        if (!Mailing.fromEntity(entity).canSend()) {
            throw new MailingSendNotAllowedException(entity.getStatus());
        }
        mailingDispatchService.dispatch(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Mailing getMailingById(GetMailingByIdQuery query) {
        return Mailing.fromEntity(requireMailing(query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Mailing> getMailings(GetMailingsQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<MailingEntity> spec = (root, cq, cb) -> {
            Predicate predicate = cb.conjunction();
            if (query.status() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), parseMailingStatus(query.status())));
            }
            if (query.type() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), parseMailingType(query.type())));
            }
            return predicate;
        };

        return mailingRepository.findAll(spec, pageable).map(Mailing::fromEntity);
    }

    private Mailing createMailingAggregate(String name,
                                           MailingType type,
                                           Long emailTemplateId,
                                           java.time.LocalDateTime executeAt,
                                           List<Long> distributionGroupIds,
                                           MailingScheduleDefinition schedule) {
        try {
            return Mailing.create(name, type, emailTemplateId, executeAt, distributionGroupIds, schedule);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidMailingConfigurationException(ex.getMessage());
        }
    }

    private MailingEntity requireMailing(Long id) {
        return mailingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new MailingNotFoundException(id));
    }

    private EmailTemplateEntity requireTemplate(Long id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new EmailTemplateNotFoundException(id));
    }

    private List<DistributionGroupEntity> requireGroups(List<Long> ids) {
        List<DistributionGroupEntity> groups = distributionGroupRepository.findByIdIn(ids);
        if (groups.size() != ids.size()) {
            throw new DistributionGroupsNotFoundException();
        }
        return groups;
    }

    private MailingType parseMailingType(String value) {
        try {
            return MailingType.fromApiValue(value);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("mailing type", value);
        }
    }

    private MailingStatus parseMailingStatus(String value) {
        try {
            return MailingStatus.fromApiValue(value);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("mailing status", value);
        }
    }
}
