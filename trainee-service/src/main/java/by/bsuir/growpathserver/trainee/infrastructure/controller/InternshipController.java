package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.InternshipsApi;
import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.RoadmapListResponse;
import by.bsuir.growpathserver.dto.model.RoadmapResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.service.RoadmapApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternshipController extends BaseController implements InternshipsApi {

    private final RoadmapApplicationFacade roadmapApplicationFacade;

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> changeInternshipStageStatus(String internshipId,
                                                                     String stageId,
                                                                     ChangeStageStatusRequest changeStageStatusRequest) {
        return ResponseEntity.ok(
                roadmapApplicationFacade.changeInternshipStageStatus(internshipId, stageId, changeStageStatusRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapResponse> createInternship(CreateRoadmapRequest createRoadmapRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapApplicationFacade.createInternship(createRoadmapRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageResponse> createInternshipStage(String internshipId,
                                                               CreateStageRequest createStageRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapApplicationFacade.createInternshipStage(internshipId, createStageRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteInternship(String internshipId) {
        return ResponseEntity.ok(roadmapApplicationFacade.deleteInternship(internshipId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteInternshipStage(String internshipId, String stageId) {
        return ResponseEntity.ok(roadmapApplicationFacade.deleteInternshipStage(internshipId, stageId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapResponse> getInternshipById(String internshipId) {
        return ResponseEntity.ok(roadmapApplicationFacade.getInternshipById(internshipId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapResponse> getInternshipRoadmap(String internshipId) {
        return ResponseEntity.ok(roadmapApplicationFacade.getInternshipById(internshipId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<StageListResponse> getInternshipStages(String internshipId) {
        return ResponseEntity.ok(roadmapApplicationFacade.getInternshipStages(internshipId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<RoadmapListResponse> listInternships(Long mentorId, Long internId, Long programId) {
        return ResponseEntity.ok(roadmapApplicationFacade.listInternships(mentorId, internId, programId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'INTERN')")
    public ResponseEntity<RoadmapListResponse> listMyInternships() {
        return ResponseEntity.ok(roadmapApplicationFacade.listMyInternships());
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER')")
    public ResponseEntity<MessageResponse> reorderInternshipStages(String internshipId,
                                                                   ReorderStagesRequest reorderStagesRequest) {
        return ResponseEntity.ok(roadmapApplicationFacade.reorderInternshipStages(internshipId, reorderStagesRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER')")
    public ResponseEntity<RoadmapResponse> updateInternship(String internshipId,
                                                            UpdateRoadmapRequest updateRoadmapRequest) {
        return ResponseEntity.ok(roadmapApplicationFacade.updateInternship(internshipId, updateRoadmapRequest));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER')")
    public ResponseEntity<StageResponse> updateInternshipStage(String internshipId,
                                                               String stageId,
                                                               UpdateStageRequest updateStageRequest) {
        return ResponseEntity.ok(
                roadmapApplicationFacade.updateInternshipStage(internshipId, stageId, updateStageRequest));
    }
}
