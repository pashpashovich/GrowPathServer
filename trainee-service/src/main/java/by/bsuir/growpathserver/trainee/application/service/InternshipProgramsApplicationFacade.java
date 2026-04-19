package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.InternshipProgramListResponse;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.handler.CreateInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.handler.DeleteInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternshipProgramByIdHandler;
import by.bsuir.growpathserver.trainee.application.handler.GetInternshipProgramsHandler;
import by.bsuir.growpathserver.trainee.application.handler.UpdateInternshipProgramHandler;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternshipProgramMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipProgramsApplicationFacade {
    private static final String MSG_PROGRAM_DELETED = "Internship program deleted successfully";
    private final CreateInternshipProgramHandler createInternshipProgramHandler;
    private final GetInternshipProgramByIdHandler getInternshipProgramByIdHandler;
    private final GetInternshipProgramsHandler getInternshipProgramsHandler;
    private final UpdateInternshipProgramHandler updateInternshipProgramHandler;
    private final DeleteInternshipProgramHandler deleteInternshipProgramHandler;
    private final InternshipProgramMapper internshipProgramMapper;
    private final ObjectMapper objectMapper;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;

    public InternshipProgramResponse createInternshipProgram(CreateInternshipProgramRequest request) {
        Long createdBy = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                               "Authenticated user is not linked to an application account"));

        CreateInternshipProgramCommand command = CreateInternshipProgramCommand.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .duration(request.getDuration())
                .maxPlaces(request.getMaxPlaces())
                .itDirectionId(request.getItDirectionId())
                .competencyIds(copyIdList(request.getCompetencyIds()))
                .requirementIds(copyIdList(request.getRequirementIds()))
                .goalIds(copyIdList(request.getGoalIds()))
                .selectionStageIds(copyIdList(request.getSelectionStageIds()))
                .status(Objects.nonNull(request.getStatus()) ?
                                InternshipProgramStatus.fromString(request.getStatus().getValue()) :
                                null)
                .createdBy(createdBy)
                .build();

        InternshipProgram program = createInternshipProgramHandler.handle(command);
        return internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
    }

    public MessageResponse deleteInternshipProgram(String id) {
        deleteInternshipProgramHandler.handle(new DeleteInternshipProgramCommand(Long.parseLong(id)));
        MessageResponse response = new MessageResponse();
        response.setMessage(MSG_PROGRAM_DELETED);
        return response;
    }

    public InternshipProgramResponse getInternshipProgramById(String id) {
        GetInternshipProgramByIdQuery query = new GetInternshipProgramByIdQuery(Long.parseLong(id));
        InternshipProgram program = getInternshipProgramByIdHandler.handle(query);
        return internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
    }

    public InternshipProgramListResponse getInternshipPrograms(Integer page,
                                                               Integer limit,
                                                               String status,
                                                               String search,
                                                               String itDirection,
                                                               LocalDate startDateFrom,
                                                               LocalDate startDateTo,
                                                               Integer maxPlacesMin,
                                                               Integer maxPlacesMax,
                                                               Long competencyId,
                                                               Boolean includeArchived) {
        GetInternshipProgramsQuery query = GetInternshipProgramsQuery.builder()
                .page(page)
                .limit(limit)
                .status(Objects.nonNull(status) ? InternshipProgramStatus.fromString(status) : null)
                .search(search)
                .itDirection(itDirection)
                .startDateFrom(startDateFrom)
                .startDateTo(startDateTo)
                .maxPlacesMin(maxPlacesMin)
                .maxPlacesMax(maxPlacesMax)
                .competencyId(competencyId)
                .includeArchived(includeArchived)
                .build();
        Page<InternshipProgram> programsPage = getInternshipProgramsHandler.handle(query);
        InternshipProgramListResponse response = new InternshipProgramListResponse();
        response.setData(programsPage.getContent().stream()
                                 .map(program -> internshipProgramMapper.toInternshipProgramResponse(program,
                                                                                                     objectMapper))
                                 .toList());
        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(programsPage.getNumber() + 1);
        pagination.setLimit(programsPage.getSize());
        pagination.setTotal((int) programsPage.getTotalElements());
        pagination.setTotalPages(programsPage.getTotalPages());
        response.setPagination(pagination);
        return response;
    }

    public InternshipProgramResponse updateInternshipProgram(String id, UpdateInternshipProgramRequest request) {
        Long programId = Long.parseLong(id);

        UpdateInternshipProgramCommand command = UpdateInternshipProgramCommand.builder()
                .id(programId)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .duration(request.getDuration())
                .maxPlaces(request.getMaxPlaces())
                .itDirectionId(request.getItDirectionId())
                .competencyIds(copyIdList(request.getCompetencyIds()))
                .requirementIds(copyIdList(request.getRequirementIds()))
                .goalIds(copyIdList(request.getGoalIds()))
                .selectionStageIds(copyIdList(request.getSelectionStageIds()))
                .status(request.getStatus() != null ?
                                InternshipProgramStatus.fromString(request.getStatus().getValue()) :
                                null)
                .build();
        InternshipProgram program = updateInternshipProgramHandler.handle(command);
        return internshipProgramMapper.toInternshipProgramResponse(program, objectMapper);
    }

    private static List<Long> copyIdList(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        return List.copyOf(ids);
    }
}
