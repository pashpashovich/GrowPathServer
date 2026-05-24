package by.bsuir.growpathserver.notification.application.service;

import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.notification.application.query.GetMailingHistoryQuery;
import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;

public interface MailingHistoryService {

    Page<MailingHistoryEntity> getMailingHistory(GetMailingHistoryQuery query);

    MailingHistoryEntity getMailingHistoryById(Long id);
}
