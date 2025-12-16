package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.UpdateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.service.AssessmentService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateAssessmentHandler {

    private final AssessmentService assessmentService;

    public Assessment handle(UpdateAssessmentCommand command) {
        return assessmentService.updateAssessment(command);
    }
}
