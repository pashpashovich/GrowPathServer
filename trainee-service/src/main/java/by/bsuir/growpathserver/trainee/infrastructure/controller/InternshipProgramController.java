package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.api.InternshipProgramsApi;
import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.InternshipProgramListResponse;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoal;
import by.bsuir.growpathserver.dto.model.SelectionStage;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternshipProgramByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternshipProgramsHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternshipProgramMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternshipProgramController implements InternshipProgramsApi {

    private final CreateInternshipProgramHandler createInternshipProgramHandler;
    private final GetInternshipProgramByIdHandler getInternshipProgramByIdHandler;
    private final GetInternshipProgramsHandler getInternshipProgramsHandler;
    private final UpdateInternshipProgramHandler updateInternshipProgramHandler;
    private final DeleteInternshipProgramHandler deleteInternshipProgramHandler;
    private final InternshipProgramMapper internshipProgramMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseEntity<InternshipProgramResponse> createInternshipProgram(CreateInternshipProgramRequest request) {
        try {
            // TODO: Get createdBy from JWT token when authentication is properly configured
            Long createdBy = 1L; // Should be extracted from JWT

            List<CreateInternshipProgramCommand.ProgramGoal> goals = null;
            if (request.getGoals() != null && !request.getGoals().isEmpty()) {
                goals = new ArrayList<>();
                for (Object goalObj : request.getGoals()) {
                    ProgramGoal goal = objectMapper.convertValue(goalObj, ProgramGoal.class);
                    goals.add(new CreateInternshipProgramCommand.ProgramGoal(goal.getTitle(), goal.getDescription()));
                }
            }

            List<CreateInternshipProgramCommand.SelectionStage> selectionStages = null;
            if (request.getSelectionStages() != null && !request.getSelectionStages().isEmpty()) {
                selectionStages = new ArrayList<>();
                for (Object stageObj : request.getSelectionStages()) {
                    SelectionStage stage = objectMapper.convertValue(stageObj, SelectionStage.class);
                    selectionStages.add(new CreateInternshipProgramCommand.SelectionStage(
                            stage.getName(), stage.getDescription(), stage.getOrder()));
                }
            }

            CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startDate(request.getStartDate())
                    .duration(request.getDuration())
                    .maxPlaces(request.getMaxPlaces())
                    .requirements(request.getRequirements())
                    .goals(goals)
                    .competencies(request.getCompetencies())
                    .selectionStages(selectionStages)
                    .status(request.getStatus() != null ?
                                    InternshipProgramStatus.fromString(request.getStatus().getValue()) :
                                    null)
                    .createdBy(createdBy)
                    .build();

            InternshipProgram program = createInternshipProgramHandler.handle(command);
            InternshipProgramResponse response = internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteInternshipProgram(String id) {
        try {
            Long programId = Long.parseLong(id);
            DeleteInternshipProgramCommand command = new DeleteInternshipProgramCommand(programId);
            deleteInternshipProgramHandler.handle(command);

            MessageResponse response = new MessageResponse();
            response.setMessage("Internship program deleted successfully");
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
    public ResponseEntity<InternshipProgramResponse> getInternshipProgramById(String id) {
        try {
            Long programId = Long.parseLong(id);
            GetInternshipProgramByIdQuery query = new GetInternshipProgramByIdQuery(programId);
            InternshipProgram program = getInternshipProgramByIdHandler.handle(query);
            InternshipProgramResponse response = internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
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
    public ResponseEntity<InternshipProgramListResponse> getInternshipPrograms(Integer page,
                                                                               Integer limit,
                                                                               String status,
                                                                               String search) {
        try {
            GetInternshipProgramsQuery query = GetInternshipProgramsQuery.builder()
                    .page(page)
                    .limit(limit)
                    .status(status != null ? InternshipProgramStatus.fromString(status) : null)
                    .search(search)
                    .build();

            Page<InternshipProgram> programsPage = getInternshipProgramsHandler.handle(query);

            InternshipProgramListResponse response = new InternshipProgramListResponse();
            response.setData(programsPage.getContent().stream()
                                     .map(program -> internshipProgramMapper.toInternshipProgramResponse(program, objectMapper))
                                     .collect(Collectors.toList()));

            PaginationResponse pagination = new PaginationResponse();
            pagination.setPage(programsPage.getNumber() + 1);
            pagination.setLimit(programsPage.getSize());
            pagination.setTotal((int) programsPage.getTotalElements());
            pagination.setTotalPages(programsPage.getTotalPages());
            response.setPagination(pagination);

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InternshipProgramResponse> updateInternshipProgram(String id,
                                                                             UpdateInternshipProgramRequest request) {
        try {
            Long programId = Long.parseLong(id);

            List<UpdateInternshipProgramCommand.ProgramGoal> goals = null;
            if (request.getGoals() != null && !request.getGoals().isEmpty()) {
                goals = new ArrayList<>();
                for (Object goalObj : request.getGoals()) {
                    ProgramGoal goal = objectMapper.convertValue(goalObj, ProgramGoal.class);
                    goals.add(new UpdateInternshipProgramCommand.ProgramGoal(goal.getTitle(), goal.getDescription()));
                }
            }

            List<UpdateInternshipProgramCommand.SelectionStage> selectionStages = null;

            UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                    .id(programId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startDate(request.getStartDate())
                    .duration(request.getDuration())
                    .maxPlaces(request.getMaxPlaces())
                    .requirements(request.getRequirements())
                    .goals(goals)
                    .competencies(request.getCompetencies())
                    .selectionStages(selectionStages)
                    .status(request.getStatus() != null ?
                                    InternshipProgramStatus.fromString(request.getStatus().getValue()) :
                                    null)
                    .build();

            InternshipProgram program = updateInternshipProgramHandler.handle(command);
            InternshipProgramResponse response = internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
