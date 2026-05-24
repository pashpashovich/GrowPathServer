package by.bsuir.growpathserver.notification.application.service;

import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.MailingHistoryListResponse;
import by.bsuir.growpathserver.dto.model.MailingHistoryResponse;
import by.bsuir.growpathserver.notification.application.query.GetMailingHistoryQuery;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.infrastructure.mapper.MailingHistoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailingHistoryApplicationFacade {

    private final MailingHistoryService mailingHistoryService;
    private final MailingHistoryMapper mailingHistoryMapper;

    public MailingHistoryListResponse getMailingHistory(Integer page, Integer limit, String mailingId, String type) {
        Long parsedMailingId = mailingId != null && !mailingId.isBlank()
                ? NotificationIds.parseRequired(mailingId, "mailingId")
                : null;
        return mailingHistoryMapper.toMailingHistoryListResponse(
                mailingHistoryService.getMailingHistory(GetMailingHistoryQuery.builder()
                                                                .page(page)
                                                                .limit(limit)
                                                                .mailingId(parsedMailingId)
                                                                .type(type)
                                                                .build()));
    }

    public MailingHistoryResponse getMailingHistoryById(String id) {
        return mailingHistoryMapper.toMailingHistoryResponse(
                mailingHistoryService.getMailingHistoryById(NotificationIds.parseRequired(id, "id")));
    }
}
