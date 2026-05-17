package by.bsuir.growpathserver.notification.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.RecipientsApi;
import by.bsuir.growpathserver.dto.model.CreateRecipientRequest;
import by.bsuir.growpathserver.dto.model.RecipientListResponse;
import by.bsuir.growpathserver.dto.model.RecipientResponse;
import by.bsuir.growpathserver.dto.model.UpdateRecipientRequest;
import by.bsuir.growpathserver.notification.application.service.RecipientApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'MENTOR', 'DEPARTMENT_HEAD', 'ADMIN')")
public class RecipientController implements RecipientsApi {

    private final RecipientApplicationFacade recipientApplicationFacade;

    @Override
    public ResponseEntity<RecipientResponse> createRecipient(CreateRecipientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipientApplicationFacade.createRecipient(request));
    }

    @Override
    public ResponseEntity<Void> deleteRecipient(String id) {
        recipientApplicationFacade.deleteRecipient(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RecipientResponse> getRecipientById(String id) {
        return ResponseEntity.ok(recipientApplicationFacade.getRecipientById(id));
    }

    @Override
    public ResponseEntity<RecipientListResponse> getRecipients(Integer page, Integer limit, String type) {
        return ResponseEntity.ok(recipientApplicationFacade.getRecipients(page, limit, type));
    }

    @Override
    public ResponseEntity<RecipientResponse> updateRecipient(String id, UpdateRecipientRequest request) {
        return ResponseEntity.ok(recipientApplicationFacade.updateRecipient(id, request));
    }
}
