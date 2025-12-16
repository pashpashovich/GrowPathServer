package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.DeleteAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.service.AssessmentService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteAssessmentHandler {

    private final AssessmentService assessmentService;

    public void handle(DeleteAssessmentCommand command) {
        assessmentService.deleteAssessment(command);
    }
}
