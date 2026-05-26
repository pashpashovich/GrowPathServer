package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.api.AssessmentsApi;
import by.bsuir.growpathserver.dto.model.AssessmentListResponse;
import by.bsuir.growpathserver.dto.model.AssessmentResponse;
import by.bsuir.growpathserver.dto.model.CreateAssessmentRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.UpdateAssessmentRequest;
import by.bsuir.growpathserver.trainee.application.command.CreateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateAssessmentHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteAssessmentHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetAssessmentByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetAssessmentsHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateAssessmentHandler;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetAssessmentByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetAssessmentsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.AssessmentResponseEnricher;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssessmentController extends BaseController implements AssessmentsApi {

    private final CreateAssessmentHandler createAssessmentHandler;
    private final GetAssessmentsHandler getAssessmentsHandler;
    private final GetAssessmentByIdHandler getAssessmentByIdHandler;
    private final UpdateAssessmentHandler updateAssessmentHandler;
    private final DeleteAssessmentHandler deleteAssessmentHandler;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final AssessmentResponseEnricher assessmentResponseEnricher;

    @Override
    public ResponseEntity<AssessmentResponse> createAssessment(CreateAssessmentRequest createAssessmentRequest) {
        try {
            Long mentorId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

            CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                    .internId(createAssessmentRequest.getInternId())
                    .mentorId(mentorId)
                    .internshipId(createAssessmentRequest.getInternshipId())
                    .iprStageId(createAssessmentRequest.getIprStageId())
                    .overallRating(createAssessmentRequest.getOverallRating())
                    .qualityRating(createAssessmentRequest.getQualityRating())
                    .speedRating(createAssessmentRequest.getSpeedRating())
                    .communicationRating(createAssessmentRequest.getCommunicationRating())
                    .comment(createAssessmentRequest.getComment())
                    .build();

            Assessment assessment = createAssessmentHandler.handle(command);
            AssessmentResponse response = assessmentResponseEnricher.toAssessmentResponse(assessment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAssessment(String id) {
        try {
            Long assessmentId = Long.parseLong(id);
            DeleteAssessmentCommand command = new DeleteAssessmentCommand(assessmentId);
            deleteAssessmentHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("Assessment deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<AssessmentResponse> getAssessmentById(String id) {
        try {
            Long assessmentId = Long.parseLong(id);
            GetAssessmentByIdQuery query = new GetAssessmentByIdQuery(assessmentId);
            Assessment assessment = getAssessmentByIdHandler.handle(query);
            AssessmentResponse response = assessmentResponseEnricher.toAssessmentResponse(assessment);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<AssessmentListResponse> getAssessments(Integer page,
                                                                 Integer limit,
                                                                 String internId,
                                                                 String mentorId,
                                                                 String internshipId,
                                                                 Long iprId,
                                                                 Long iprStageId) {
        try {
            GetAssessmentsQuery query = GetAssessmentsQuery.builder()
                    .page(page)
                    .limit(limit)
                    .internId(internId)
                    .mentorId(mentorId)
                    .internshipId(internshipId)
                    .iprId(iprId)
                    .iprStageId(iprStageId)
                    .build();

            Page<Assessment> assessmentsPage = getAssessmentsHandler.handle(query);

            AssessmentListResponse response = new AssessmentListResponse();
            response.setData(assessmentsPage.getContent().stream()
                                     .map(assessmentResponseEnricher::toAssessmentResponse)
                                     .toList());

            PaginationResponse pagination = new PaginationResponse();
            pagination.setPage(assessmentsPage.getNumber() + 1);
            pagination.setLimit(assessmentsPage.getSize());
            pagination.setTotal((int) assessmentsPage.getTotalElements());
            pagination.setTotalPages(assessmentsPage.getTotalPages());
            response.setPagination(pagination);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<AssessmentResponse> updateAssessment(String id,
                                                               UpdateAssessmentRequest updateAssessmentRequest) {
        try {
            Long assessmentId = Long.parseLong(id);
            UpdateAssessmentCommand command = UpdateAssessmentCommand.builder()
                    .id(assessmentId)
                    .overallRating(updateAssessmentRequest.getOverallRating())
                    .qualityRating(updateAssessmentRequest.getQualityRating())
                    .speedRating(updateAssessmentRequest.getSpeedRating())
                    .communicationRating(updateAssessmentRequest.getCommunicationRating())
                    .comment(updateAssessmentRequest.getComment())
                    .build();

            Assessment assessment = updateAssessmentHandler.handle(command);
            AssessmentResponse response = assessmentResponseEnricher.toAssessmentResponse(assessment);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
