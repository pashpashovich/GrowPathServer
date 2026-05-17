package by.bsuir.growpathserver.notification.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.DistributionGroupsApi;
import by.bsuir.growpathserver.dto.model.AddRecipientToDistributionGroupRequest;
import by.bsuir.growpathserver.dto.model.CreateDistributionGroupRequest;
import by.bsuir.growpathserver.dto.model.DistributionGroupListResponse;
import by.bsuir.growpathserver.dto.model.DistributionGroupResponse;
import by.bsuir.growpathserver.dto.model.RecipientListResponse;
import by.bsuir.growpathserver.dto.model.UpdateDistributionGroupRequest;
import by.bsuir.growpathserver.notification.application.service.DistributionGroupApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR_MANAGER', 'MENTOR', 'DEPARTMENT_HEAD', 'ADMIN')")
public class DistributionGroupController implements DistributionGroupsApi {

    private final DistributionGroupApplicationFacade distributionGroupApplicationFacade;

    @Override
    public ResponseEntity<Void> addRecipientToDistributionGroup(
            String id,
            AddRecipientToDistributionGroupRequest request) {
        distributionGroupApplicationFacade.addRecipientToDistributionGroup(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<DistributionGroupResponse> createDistributionGroup(CreateDistributionGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(distributionGroupApplicationFacade.createDistributionGroup(request));
    }

    @Override
    public ResponseEntity<Void> deleteDistributionGroup(String id) {
        distributionGroupApplicationFacade.deleteDistributionGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<DistributionGroupResponse> getDistributionGroupById(String id) {
        return ResponseEntity.ok(distributionGroupApplicationFacade.getDistributionGroupById(id));
    }

    @Override
    public ResponseEntity<RecipientListResponse> getDistributionGroupRecipients(String id) {
        return ResponseEntity.ok(distributionGroupApplicationFacade.getDistributionGroupRecipients(id));
    }

    @Override
    public ResponseEntity<DistributionGroupListResponse> getDistributionGroups(Integer page, Integer limit) {
        return ResponseEntity.ok(distributionGroupApplicationFacade.getDistributionGroups(page, limit));
    }

    @Override
    public ResponseEntity<Void> removeRecipientFromDistributionGroup(String id, String recipientId) {
        distributionGroupApplicationFacade.removeRecipientFromDistributionGroup(id, recipientId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<DistributionGroupResponse> updateDistributionGroup(
            String id,
            UpdateDistributionGroupRequest request) {
        return ResponseEntity.ok(distributionGroupApplicationFacade.updateDistributionGroup(id, request));
    }
}
