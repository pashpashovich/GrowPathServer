package by.bsuir.growpathserver.notification.application.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.command.CreateRecipientCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteRecipientCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateRecipientCommand;
import by.bsuir.growpathserver.notification.application.exception.DistributionGroupNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.DuplicateRecipientEmailException;
import by.bsuir.growpathserver.notification.application.exception.InvalidRecipientDataException;
import by.bsuir.growpathserver.notification.application.exception.RecipientNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.UnknownEnumerationValueException;
import by.bsuir.growpathserver.notification.application.query.GetRecipientByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetRecipientsQuery;
import by.bsuir.growpathserver.notification.application.service.RecipientService;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;
import by.bsuir.growpathserver.notification.domain.entity.DistributionGroupEntity;
import by.bsuir.growpathserver.notification.domain.entity.RecipientEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.RecipientType;
import by.bsuir.growpathserver.notification.infrastructure.repository.DistributionGroupRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.RecipientRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final DistributionGroupRepository distributionGroupRepository;

    @Override
    @Transactional
    public Recipient createRecipient(CreateRecipientCommand command) {
        RecipientType type = parseRecipientType(command.type());
        if (recipientRepository.existsByEmailIgnoreCase(command.email())) {
            throw new DuplicateRecipientEmailException(command.email());
        }
        Recipient recipient = createRecipientAggregate(command.email(), command.fullName(), command.userId(), type);
        RecipientEntity saved = recipientRepository.save(recipient.toEntity());
        return Recipient.fromEntity(saved);
    }

    @Override
    @Transactional
    public Recipient updateRecipient(UpdateRecipientCommand command) {
        RecipientEntity entity = requireRecipient(command.id());
        if (command.email() != null) {
            if (recipientRepository.existsByEmailIgnoreCaseAndIdNot(command.email(), command.id())) {
                throw new DuplicateRecipientEmailException(command.email());
            }
            entity.setEmail(command.email());
        }
        if (command.fullName() != null) {
            entity.setFullName(command.fullName());
        }
        if (command.userId() != null) {
            entity.setUserId(command.userId());
        }
        RecipientEntity saved = recipientRepository.save(entity);
        return Recipient.fromEntity(recipientRepository.findWithGroupsById(saved.getId()).orElse(saved));
    }

    @Override
    @Transactional
    public void deleteRecipient(DeleteRecipientCommand command) {
        RecipientEntity entity = requireRecipient(command.id());
        recipientRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Recipient getRecipientById(GetRecipientByIdQuery query) {
        return Recipient.fromEntity(requireRecipient(query.id()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Recipient> getRecipients(GetRecipientsQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<RecipientEntity> spec = (root, cq, cb) -> {
            if (query.type() == null) {
                return cb.conjunction();
            }
            Predicate predicate = cb.equal(root.get("type"), parseRecipientType(query.type()));
            return predicate;
        };

        return recipientRepository.findAll(spec, pageable)
                .map(entity -> Recipient.fromEntity(
                        recipientRepository.findWithGroupsById(entity.getId()).orElse(entity)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recipient> getRecipientsByGroupId(Long groupId) {
        DistributionGroupEntity group = distributionGroupRepository.findWithRecipientsById(groupId)
                .orElseThrow(() -> new DistributionGroupNotFoundException(groupId));
        return group.getRecipients().stream()
                .map(recipient -> recipientRepository.findWithGroupsById(recipient.getId()).orElse(recipient))
                .map(Recipient::fromEntity)
                .toList();
    }

    private RecipientEntity requireRecipient(Long id) {
        return recipientRepository.findWithGroupsById(id)
                .orElseThrow(() -> new RecipientNotFoundException(id));
    }

    private Recipient createRecipientAggregate(String email, String fullName, Long userId, RecipientType type) {
        try {
            return Recipient.create(email, fullName, userId, type);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidRecipientDataException(ex.getMessage());
        }
    }

    private RecipientType parseRecipientType(String value) {
        try {
            return RecipientType.fromApiValue(value);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("recipient type", value);
        }
    }
}
