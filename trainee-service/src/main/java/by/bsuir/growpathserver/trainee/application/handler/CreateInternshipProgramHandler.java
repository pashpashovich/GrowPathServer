package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateInternshipProgramHandler {

    private final InternshipProgramService internshipProgramService;

    public InternshipProgram handle(CreateInternshipProgramCommand command) {
        return internshipProgramService.createInternshipProgram(command);
    }
}
