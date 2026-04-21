package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.InternshipProgramsApi;
import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.InternshipProgramListResponse;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramsApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternshipProgramController extends BaseController implements InternshipProgramsApi {

    private final InternshipProgramsApplicationFacade internshipProgramsApplicationFacade;

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<InternshipProgramResponse> createInternshipProgram(CreateInternshipProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(internshipProgramsApplicationFacade.createInternshipProgram(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> deleteInternshipProgram(String id) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.deleteInternshipProgram(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<InternshipProgramResponse> getInternshipProgramById(String id) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.getInternshipProgramById(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR')")
    public ResponseEntity<InternshipProgramListResponse> getInternshipPrograms(Integer page,
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
        return ResponseEntity.ok(internshipProgramsApplicationFacade.getInternshipPrograms(
                page, limit, status, search, itDirection, startDateFrom, startDateTo,
                maxPlacesMin, maxPlacesMax, competencyId, includeArchived));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<InternshipProgramResponse> updateInternshipProgram(String id,
                                                                             UpdateInternshipProgramRequest request) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.updateInternshipProgram(id, request));
    }
}
