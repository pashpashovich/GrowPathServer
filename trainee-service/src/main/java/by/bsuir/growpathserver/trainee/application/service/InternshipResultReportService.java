package by.bsuir.growpathserver.trainee.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipResultReportService {

    private final InternshipResultReportAccessService accessService;
    private final InternshipResultReportAggregationService aggregationService;
    private final InternshipResultReportPdfRenderer pdfRenderer;

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long internId) {
        accessService.verifyCanViewReport(internId);
        InternshipResultReportData data = aggregationService.aggregate(internId);
        return pdfRenderer.render(data);
    }
}
