package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.InternsApi;
import by.bsuir.growpathserver.dto.model.AssessmentListResponse;
import by.bsuir.growpathserver.dto.model.CreateInternRequest;
import by.bsuir.growpathserver.dto.model.InternListResponse;
import by.bsuir.growpathserver.dto.model.InternProgressResponse;
import by.bsuir.growpathserver.dto.model.InternResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.dto.model.UpdateInternRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternController implements InternsApi {

    @Override
    public ResponseEntity<InternResponse> createIntern(CreateInternRequest createInternRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<MessageResponse> deleteIntern(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AssessmentListResponse> getInternAssessments(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternResponse> getInternById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternProgressResponse> getInternProgress(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<TaskListResponse> getInternTasks(String id, String status) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternListResponse> getInterns(Integer page,
                                                         Integer limit,
                                                         String search,
                                                         String department,
                                                         String status,
                                                         Double rating) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<InternResponse> updateIntern(String id, UpdateInternRequest updateInternRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
