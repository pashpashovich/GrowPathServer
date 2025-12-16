package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.AssessmentListResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.trainee.application.query.GetAssessmentsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetInternAssessmentsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.AssessmentMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternAssessmentsHandler {

    private final GetAssessmentsHandler getAssessmentsHandler;
    private final AssessmentMapper assessmentMapper;

    public AssessmentListResponse handle(GetInternAssessmentsQuery query) {
        GetAssessmentsQuery getAssessmentsQuery = GetAssessmentsQuery.builder()
                .page(query.page() != null ? query.page() : 1)
                .limit(query.limit() != null ? query.limit() : 100)
                .internId(query.internId())
                .build();

        Page<Assessment> assessmentsPage = getAssessmentsHandler.handle(getAssessmentsQuery);

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

        return response;
    }
}
