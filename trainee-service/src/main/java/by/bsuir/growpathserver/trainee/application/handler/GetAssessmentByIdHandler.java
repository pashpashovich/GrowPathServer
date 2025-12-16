package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetAssessmentByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.AssessmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetAssessmentByIdHandler {

    private final AssessmentService assessmentService;

    public Assessment handle(GetAssessmentByIdQuery query) {
        return assessmentService.getAssessmentById(query.id());
    }
}
