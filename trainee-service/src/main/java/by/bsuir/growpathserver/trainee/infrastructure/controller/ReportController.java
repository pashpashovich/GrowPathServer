package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.time.LocalDate;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ReportsApi;
import by.bsuir.growpathserver.dto.model.MentorWorkloadResponse;
import by.bsuir.growpathserver.dto.model.ReportListResponse;
import by.bsuir.growpathserver.trainee.application.service.ReportFacade;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportController extends BaseController implements ReportsApi {

    private final ReportFacade reportFacade;

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<MentorWorkloadResponse> getMentorWorkload(String mentorId) {
        return ResponseEntity.ok(reportFacade.getMentorWorkload(mentorId));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<ReportListResponse> getReports(String programId,
                                                         String mentorId,
                                                         String period,
                                                         java.time.LocalDate startDate,
                                                         java.time.LocalDate endDate) {
        return ResponseEntity.ok(reportFacade.getReports(programId, mentorId, period, startDate, endDate));
    }

    @Override
    @PreAuthorize("hasAnyRole('MENTOR', 'HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<Resource> exportReportsCsv(@RequestParam(name = "programId", required = false) String programId,
                                                     @RequestParam(name = "mentorId", required = false) String mentorId,
                                                     @RequestParam(name = "period", required = false) String period,
                                                     @RequestParam(name = "startDate", required = false) LocalDate startDate,
                                                     @RequestParam(name = "endDate", required = false) LocalDate endDate) {
        byte[] csv = reportFacade.exportReportsCsv(programId, mentorId, period, startDate, endDate);
        Resource resource = new ByteArrayResource(csv);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"internship-efficiency-report.csv\"")
                .body(resource);
    }

    @Override
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'DEPARTMENT_HEAD')")
    public ResponseEntity<Resource> exportMentorWorkloadCsv(@RequestParam(name = "mentorId", required = false) String mentorId) {
        byte[] csv = reportFacade.exportMentorWorkloadCsv(mentorId);
        Resource resource = new ByteArrayResource(csv);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mentor-workload-report.csv\"")
                .body(resource);
    }
}
