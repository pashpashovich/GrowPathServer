package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.application.command.CreateAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteAssessmentCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateAssessmentCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;

public interface AssessmentService {
    Assessment createAssessment(CreateAssessmentCommand command);

    Assessment updateAssessment(UpdateAssessmentCommand command);

    void deleteAssessment(DeleteAssessmentCommand command);

    Assessment getAssessmentById(Long id);
}
