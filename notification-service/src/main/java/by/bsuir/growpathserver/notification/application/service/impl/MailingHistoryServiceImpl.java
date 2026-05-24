package by.bsuir.growpathserver.notification.application.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.application.exception.MailingHistoryNotFoundException;
import by.bsuir.growpathserver.notification.application.exception.UnknownEnumerationValueException;
import by.bsuir.growpathserver.notification.application.query.GetMailingHistoryQuery;
import by.bsuir.growpathserver.notification.application.service.MailingHistoryService;
import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingHistoryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailingHistoryServiceImpl implements MailingHistoryService {

    private final MailingHistoryRepository mailingHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<MailingHistoryEntity> getMailingHistory(GetMailingHistoryQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "sentAt"));

        Specification<MailingHistoryEntity> spec = (root, cq, cb) -> {
            Predicate predicate = cb.conjunction();
            if (query.mailingId() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("mailingId"), query.mailingId()));
            }
            if (query.type() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("mailingType"), parseMailingType(query.type())));
            }
            return predicate;
        };

        return mailingHistoryRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public MailingHistoryEntity getMailingHistoryById(Long id) {
        return mailingHistoryRepository.findById(id)
                .orElseThrow(() -> new MailingHistoryNotFoundException(id));
    }

    private MailingType parseMailingType(String type) {
        try {
            return MailingType.fromApiValue(type);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("type", type);
        }
    }
}
