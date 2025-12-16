package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.common.util.JwtUtils;
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
import by.bsuir.growpathserver.trainee.application.query.GetAssessmentByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetAssessmentsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.AssessmentMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AssessmentController implements AssessmentsApi {

    private final CreateAssessmentHandler createAssessmentHandler;
    private final GetAssessmentsHandler getAssessmentsHandler;
    private final GetAssessmentByIdHandler getAssessmentByIdHandler;
    private final UpdateAssessmentHandler updateAssessmentHandler;
    private final DeleteAssessmentHandler deleteAssessmentHandler;
    private final AssessmentMapper assessmentMapper;

    @Override
    public ResponseEntity<AssessmentResponse> createAssessment(CreateAssessmentRequest createAssessmentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            String mentorId = JwtUtils.getUserId(jwt);
            if (mentorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long internId = Long.parseLong(createAssessmentRequest.getInternId());
            Long mentorIdLong = Long.parseLong(mentorId);
            Long internshipId = Long.parseLong(createAssessmentRequest.getInternshipId());

            CreateAssessmentCommand command = CreateAssessmentCommand.builder()
                    .internId(internId)
                    .mentorId(mentorIdLong)
                    .internshipId(internshipId)
                    .overallRating(createAssessmentRequest.getOverallRating())
                    .qualityRating(createAssessmentRequest.getQualityRating())
                    .speedRating(createAssessmentRequest.getSpeedRating())
                    .communicationRating(createAssessmentRequest.getCommunicationRating())
                    .comment(createAssessmentRequest.getComment())
                    .build();

            Assessment assessment = createAssessmentHandler.handle(command);
            AssessmentResponse response = assessmentMapper.toAssessmentResponse(assessment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            AssessmentResponse response = assessmentMapper.toAssessmentResponse(assessment);
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
                                                                 String internshipId) {
        try {
            GetAssessmentsQuery query = GetAssessmentsQuery.builder()
                    .page(page)
                    .limit(limit)
                    .internId(internId)
                    .mentorId(mentorId)
                    .internshipId(internshipId)
                    .build();

            Page<Assessment> assessmentsPage = getAssessmentsHandler.handle(query);

            AssessmentListResponse response = new AssessmentListResponse();
            response.setData(assessmentsPage.getContent().stream()
                                     .map(assessmentMapper::toAssessmentResponse)
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
            AssessmentResponse response = assessmentMapper.toAssessmentResponse(assessment);
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
