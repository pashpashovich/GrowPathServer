package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.ReportsApi;
import by.bsuir.growpathserver.dto.model.MentorWorkloadResponse;
import by.bsuir.growpathserver.dto.model.ReportListResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    @Override
    public ResponseEntity<MentorWorkloadResponse> getMentorWorkload(String mentorId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<ReportListResponse> getReports(String programId,
                                                         String mentorId,
                                                         String period,
                                                         java.time.LocalDate startDate,
                                                         java.time.LocalDate endDate) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
