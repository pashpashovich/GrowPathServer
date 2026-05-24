package by.bsuir.growpathserver.notification.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.MailingHistoryApi;
import by.bsuir.growpathserver.dto.model.MailingHistoryListResponse;
import by.bsuir.growpathserver.dto.model.MailingHistoryResponse;
import by.bsuir.growpathserver.notification.application.service.MailingHistoryApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'MENTOR', 'DEPARTMENT_HEAD', 'ADMIN')")
public class MailingHistoryController implements MailingHistoryApi {

    private final MailingHistoryApplicationFacade mailingHistoryApplicationFacade;

    @Override
    public ResponseEntity<MailingHistoryListResponse> getMailingHistory(
            Integer page,
            Integer limit,
            String mailingId,
            String type) {
        return ResponseEntity.ok(
                mailingHistoryApplicationFacade.getMailingHistory(page, limit, mailingId, type));
    }

    @Override
    public ResponseEntity<MailingHistoryResponse> getMailingHistoryById(String id) {
        return ResponseEntity.ok(mailingHistoryApplicationFacade.getMailingHistoryById(id));
    }
}
