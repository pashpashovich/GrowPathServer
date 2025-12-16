package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateInternshipProgramHandler {

    private final InternshipProgramService internshipProgramService;

    public InternshipProgram handle(UpdateInternshipProgramCommand command) {
        return internshipProgramService.updateInternshipProgram(command);
    }
}
