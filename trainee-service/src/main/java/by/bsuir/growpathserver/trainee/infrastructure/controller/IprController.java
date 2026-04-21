package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.IprsApi;
import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateIprRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.IprListResponse;
import by.bsuir.growpathserver.dto.model.IprResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateIprRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.service.IprApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class IprController extends BaseController implements IprsApi {

    private final IprApplicationFacade iprApplicationFacade;

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'INTERN', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<IprListResponse> listIprs(Long mentorId, Long internId, Long programId, Long templateId) {
        return ResponseEntity.ok(iprApplicationFacade.listIprs(mentorId, internId, programId, templateId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<IprResponse> createIpr(CreateIprRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(iprApplicationFacade.createIpr(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'INTERN')")
    public ResponseEntity<IprListResponse> listMyIprs() {
        return ResponseEntity.ok(iprApplicationFacade.listMyIprs());
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'INTERN', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<IprResponse> getIprById(String iprId) {
        return ResponseEntity.ok(iprApplicationFacade.getIprById(iprId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<IprResponse> updateIpr(String iprId, UpdateIprRequest updateIprRequest) {
        return ResponseEntity.ok(iprApplicationFacade.updateIpr(iprId, updateIprRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteIpr(String iprId) {
        return ResponseEntity.ok(iprApplicationFacade.deleteIpr(iprId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'INTERN', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageListResponse> getIprStages(String iprId) {
        return ResponseEntity.ok(iprApplicationFacade.getIprStages(iprId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> createIprStage(String iprId, CreateStageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(iprApplicationFacade.createIprStage(iprId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> updateIprStage(String iprId, String stageId, UpdateStageRequest request) {
        return ResponseEntity.ok(iprApplicationFacade.updateIprStage(iprId, stageId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteIprStage(String iprId, String stageId) {
        return ResponseEntity.ok(iprApplicationFacade.deleteIprStage(iprId, stageId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> reorderIprStages(String iprId, ReorderStagesRequest request) {
        return ResponseEntity.ok(iprApplicationFacade.reorderIprStages(iprId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> changeIprStageStatus(String iprId,
                                                              String stageId,
                                                              ChangeStageStatusRequest request) {
        return ResponseEntity.ok(iprApplicationFacade.changeIprStageStatus(iprId, stageId, request));
    }
}
