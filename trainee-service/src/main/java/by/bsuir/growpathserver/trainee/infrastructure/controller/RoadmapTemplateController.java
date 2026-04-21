package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.RoadmapTemplatesApi;
import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateRoadmapTemplateRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.RoadmapTemplateListResponse;
import by.bsuir.growpathserver.dto.model.RoadmapTemplateResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateRoadmapTemplateRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.service.RoadmapApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoadmapTemplateController extends BaseController implements RoadmapTemplatesApi {

    private final RoadmapApplicationFacade roadmapApplicationFacade;

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapTemplateListResponse> listRoadmapTemplates(Long programId, Long mentorId) {
        return ResponseEntity.ok(roadmapApplicationFacade.listRoadmapTemplates(mentorId, programId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapTemplateResponse> createRoadmapTemplate(CreateRoadmapTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roadmapApplicationFacade.createRoadmapTemplate(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapTemplateResponse> getRoadmapTemplateById(String templateId) {
        return ResponseEntity.ok(roadmapApplicationFacade.getRoadmapTemplateById(templateId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapTemplateResponse> updateRoadmapTemplate(String templateId,
                                                                         UpdateRoadmapTemplateRequest request) {
        return ResponseEntity.ok(roadmapApplicationFacade.updateRoadmapTemplate(templateId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteRoadmapTemplate(String templateId) {
        return ResponseEntity.ok(roadmapApplicationFacade.deleteRoadmapTemplate(templateId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageListResponse> getRoadmapTemplateStages(String templateId) {
        return ResponseEntity.ok(roadmapApplicationFacade.getRoadmapTemplateStages(templateId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> createRoadmapTemplateStage(String templateId, CreateStageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapApplicationFacade.createRoadmapTemplateStage(templateId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> updateRoadmapTemplateStage(String templateId,
                                                                    String stageId,
                                                                    UpdateStageRequest request) {
        return ResponseEntity.ok(roadmapApplicationFacade.updateRoadmapTemplateStage(templateId, stageId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteRoadmapTemplateStage(String templateId, String stageId) {
        return ResponseEntity.ok(roadmapApplicationFacade.deleteRoadmapTemplateStage(templateId, stageId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> reorderRoadmapTemplateStages(String templateId,
                                                                        ReorderStagesRequest request) {
        return ResponseEntity.ok(roadmapApplicationFacade.reorderRoadmapTemplateStages(templateId, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> changeRoadmapTemplateStageStatus(String templateId,
                                                                          String stageId,
                                                                          ChangeStageStatusRequest request) {
        return ResponseEntity.ok(
                roadmapApplicationFacade.changeRoadmapTemplateStageStatus(templateId, stageId, request));
    }
}
