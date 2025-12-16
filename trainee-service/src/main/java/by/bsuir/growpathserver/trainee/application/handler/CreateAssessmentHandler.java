package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CreateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.service.AssessmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateAssessmentHandler {

    private final AssessmentService assessmentService;

    public Assessment handle(CreateAssessmentCommand command) {
        return assessmentService.createAssessment(command);
    }
}
