package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;

public interface InternshipProgramService {
    InternshipProgram createInternshipProgram(CreateInternshipProgramCommand command);

    InternshipProgram updateInternshipProgram(UpdateInternshipProgramCommand command);

    void deleteInternshipProgram(DeleteInternshipProgramCommand command);

    InternshipProgram getInternshipProgramById(Long id);
}
