package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.time.LocalDate;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.InternshipProgramsApi;
import by.bsuir.growpathserver.dto.model.CreateInternshipProgramRequest;
import by.bsuir.growpathserver.dto.model.InternshipProgramListResponse;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.UpdateInternshipProgramRequest;
import by.bsuir.growpathserver.trainee.application.service.InternshipEfficiencyReportService;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramsApplicationFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InternshipProgramController extends BaseController implements InternshipProgramsApi {

    private final InternshipProgramsApplicationFacade internshipProgramsApplicationFacade;
    private final InternshipEfficiencyReportService internshipEfficiencyReportService;

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<InternshipProgramResponse> createInternshipProgram(CreateInternshipProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(internshipProgramsApplicationFacade.createInternshipProgram(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<MessageResponse> deleteInternshipProgram(String id) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.deleteInternshipProgram(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<InternshipProgramResponse> getInternshipProgramById(String id) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.getInternshipProgramById(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<Resource> downloadInternshipEfficiencyReport(String id) {
        try {
            Long programId = Long.parseLong(id);
            byte[] pdf = internshipEfficiencyReportService.generatePdf(programId);
            Resource resource = new ByteArrayResource(pdf);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"internship-efficiency-report-" + programId + ".pdf\"")
                    .body(resource);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'MENTOR', 'DEPARTMENT_HEAD')")
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
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<InternshipProgramResponse> updateInternshipProgram(String id,
                                                                             UpdateInternshipProgramRequest request) {
        return ResponseEntity.ok(internshipProgramsApplicationFacade.updateInternshipProgram(id, request));
    }
}
