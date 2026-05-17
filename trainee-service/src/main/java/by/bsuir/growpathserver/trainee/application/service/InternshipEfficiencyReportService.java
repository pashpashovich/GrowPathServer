package by.bsuir.growpathserver.trainee.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.report.InternshipEfficiencyReportData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipEfficiencyReportService {

    private final InternshipEfficiencyReportAggregationService aggregationService;
    private final InternshipEfficiencyReportPdfRenderer pdfRenderer;

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long programId) {
        InternshipEfficiencyReportData data = aggregationService.aggregate(programId);
        return pdfRenderer.render(data);
    }
}
