package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteInternshipProgramHandler {

    private final InternshipProgramService internshipProgramService;

    public void handle(DeleteInternshipProgramCommand command) {
        internshipProgramService.deleteInternshipProgram(command);
    }
}
