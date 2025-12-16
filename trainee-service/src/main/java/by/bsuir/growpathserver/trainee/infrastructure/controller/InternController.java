package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.NoSuchElementException;

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
import by.bsuir.growpathserver.trainee.application.command.CreateInternCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteUserCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateInternHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteUserHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternAssessmentsHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternProgressHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternTasksHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternsHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateInternHandler;
import by.bsuir.growpathserver.trainee.application.query.GetInternAssessmentsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternProgressQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternTasksQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternController implements InternsApi {

    private final CreateInternHandler createInternHandler;
    private final GetInternsHandler getInternsHandler;
    private final GetInternByIdHandler getInternByIdHandler;
    private final UpdateInternHandler updateInternHandler;
    private final DeleteUserHandler deleteUserHandler;
    private final GetInternTasksHandler getInternTasksHandler;
    private final GetInternAssessmentsHandler getInternAssessmentsHandler;
    private final GetInternProgressHandler getInternProgressHandler;
    private final InternMapper internMapper;

    @Override
    public ResponseEntity<InternResponse> createIntern(CreateInternRequest createInternRequest) {
        try {
            CreateInternCommand command = CreateInternCommand.builder()
                    .userId(createInternRequest.getUserId())
                    .department(createInternRequest.getDepartment())
                    .position(createInternRequest.getPosition())
                    .internshipProgramId(createInternRequest.getInternshipProgramId())
                    .mentorId(createInternRequest.getMentorId())
                    .build();

            User user = createInternHandler.handle(command);
            InternResponse response = internMapper.toInternResponse(user);

            if (createInternRequest.getDepartment() != null) {
                response.setDepartment(createInternRequest.getDepartment());
            }
            if (createInternRequest.getPosition() != null) {
                response.setPosition(createInternRequest.getPosition());
            }
            if (createInternRequest.getInternshipProgramId() != null) {
                response.setInternshipProgramId(createInternRequest.getInternshipProgramId());
            }
            if (createInternRequest.getMentorId() != null) {
                response.setMentorId(createInternRequest.getMentorId());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteIntern(String id) {
        try {
            Long userId = Long.parseLong(id);
            DeleteUserCommand command = new DeleteUserCommand(userId);
            deleteUserHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("Intern deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<AssessmentListResponse> getInternAssessments(String id) {
        try {
            GetInternAssessmentsQuery query = GetInternAssessmentsQuery.builder()
                    .internId(id)
                    .page(1)
                    .limit(100)
                    .build();

            AssessmentListResponse response = getInternAssessmentsHandler.handle(query);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InternResponse> getInternById(String id) {
        try {
            GetInternByIdQuery query = new GetInternByIdQuery(id);
            User user = getInternByIdHandler.handle(query);
            InternResponse response = internMapper.toInternResponse(user);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<InternProgressResponse> getInternProgress(String id) {
        try {
            GetInternProgressQuery query = new GetInternProgressQuery(id);
            InternProgressResponse response = getInternProgressHandler.handle(query);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<TaskListResponse> getInternTasks(String id, String status) {
        try {
            GetInternTasksQuery query = GetInternTasksQuery.builder()
                    .internId(id)
                    .status(status)
                    .build();

            TaskListResponse response = getInternTasksHandler.handle(query);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InternListResponse> getInterns(Integer page,
                                                         Integer limit,
                                                         String search,
                                                         String department,
                                                         String status,
                                                         Double rating) {
        try {
            GetInternsQuery query = GetInternsQuery.builder()
                    .page(page)
                    .limit(limit)
                    .search(search)
                    .department(department)
                    .status(status)
                    .rating(rating)
                    .build();

            InternListResponse response = getInternsHandler.handle(query);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InternResponse> updateIntern(String id, UpdateInternRequest updateInternRequest) {
        try {
            UpdateInternCommand command = UpdateInternCommand.builder()
                    .internId(id)
                    .department(updateInternRequest.getDepartment())
                    .position(updateInternRequest.getPosition())
                    .status(updateInternRequest.getStatus() != null ? updateInternRequest.getStatus().getValue() : null)
                    .mentorId(updateInternRequest.getMentorId())
                    .build();

            InternResponse response = updateInternHandler.handle(command);
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
