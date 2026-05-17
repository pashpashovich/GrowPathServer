package by.bsuir.growpathserver.notification.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.MailingsApi;
import by.bsuir.growpathserver.dto.model.CreateMailingRequest;
import by.bsuir.growpathserver.dto.model.MailingListResponse;
import by.bsuir.growpathserver.dto.model.MailingResponse;
import by.bsuir.growpathserver.dto.model.UpdateMailingRequest;
import by.bsuir.growpathserver.notification.application.service.MailingApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'MENTOR', 'DEPARTMENT_HEAD', 'ADMIN')")
public class MailingController implements MailingsApi {

    private final MailingApplicationFacade mailingApplicationFacade;

    @Override
    public ResponseEntity<MailingResponse> createMailing(CreateMailingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mailingApplicationFacade.createMailing(request));
    }

    @Override
    public ResponseEntity<Void> deleteMailing(String id) {
        mailingApplicationFacade.deleteMailing(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MailingResponse> getMailingById(String id) {
        return ResponseEntity.ok(mailingApplicationFacade.getMailingById(id));
    }

    @Override
    public ResponseEntity<MailingListResponse> getMailings(Integer page, Integer limit, String status, String type) {
        return ResponseEntity.ok(mailingApplicationFacade.getMailings(page, limit, status, type));
    }

    @Override
    public ResponseEntity<Void> sendMailing(String id) {
        mailingApplicationFacade.sendMailing(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<MailingResponse> updateMailing(String id, UpdateMailingRequest request) {
        return ResponseEntity.ok(mailingApplicationFacade.updateMailing(id, request));
    }
}
