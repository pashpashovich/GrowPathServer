package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.RoadmapsApi;
import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.RoadmapResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoadmapController implements RoadmapsApi {

    @Override
    public ResponseEntity<StageResponse> changeStageStatus(String roadmapId,
                                                           String stageId,
                                                           ChangeStageStatusRequest changeStageStatusRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<RoadmapResponse> createRoadmap(CreateRoadmapRequest createRoadmapRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<StageResponse> createStage(String roadmapId, CreateStageRequest createStageRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteRoadmap(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteStage(String roadmapId, String stageId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<RoadmapResponse> getRoadmapById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<StageListResponse> getRoadmapStages(String roadmapId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> reorderStages(String roadmapId, ReorderStagesRequest reorderStagesRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<RoadmapResponse> updateRoadmap(String id, UpdateRoadmapRequest updateRoadmapRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<StageResponse> updateStage(String roadmapId,
                                                     String stageId,
                                                     UpdateStageRequest updateStageRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
