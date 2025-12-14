package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.AssessmentsApi;
import by.bsuir.growpathserver.dto.model.AssessmentListResponse;
import by.bsuir.growpathserver.dto.model.AssessmentResponse;
import by.bsuir.growpathserver.dto.model.CreateAssessmentRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateAssessmentRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssessmentController implements AssessmentsApi {

    @Override
    public ResponseEntity<AssessmentResponse> createAssessment(CreateAssessmentRequest createAssessmentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAssessment(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AssessmentResponse> getAssessmentById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AssessmentListResponse> getAssessments(Integer page,
                                                                 Integer limit,
                                                                 String internId,
                                                                 String mentorId,
                                                                 String internshipId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AssessmentResponse> updateAssessment(String id,
                                                               UpdateAssessmentRequest updateAssessmentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
