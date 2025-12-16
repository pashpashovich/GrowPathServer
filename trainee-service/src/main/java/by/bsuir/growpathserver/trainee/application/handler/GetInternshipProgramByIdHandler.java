package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternshipProgramByIdHandler {

    private final InternshipProgramService internshipProgramService;

    public InternshipProgram handle(GetInternshipProgramByIdQuery query) {
        return internshipProgramService.getInternshipProgramById(query.id());
    }
}
