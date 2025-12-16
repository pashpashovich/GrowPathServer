package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.InternResponse;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternCommand;
import by.bsuir.growpathserver.trainee.application.query.GetInternByIdQuery;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateInternHandler {

    private final GetInternByIdHandler getInternByIdHandler;
    private final InternMapper internMapper;

    public InternResponse handle(UpdateInternCommand command) {
        GetInternByIdQuery query = new GetInternByIdQuery(command.internId());
        var user = getInternByIdHandler.handle(query);
        InternResponse response = internMapper.toInternResponse(user);

        if (command.department() != null) {
            response.setDepartment(command.department());
        }
        if (command.position() != null) {
            response.setPosition(command.position());
        }
        if (command.status() != null) {
            response.setStatus(convertStatus(command.status()));
        }
        if (command.mentorId() != null) {
            response.setMentorId(command.mentorId());
        }

        return response;
    }

    private InternResponse.StatusEnum convertStatus(String status) {
        if (status == null) {
            return null;
        }
        return switch (status.toLowerCase()) {
            case "active" -> InternResponse.StatusEnum.ACTIVE;
            case "completed" -> InternResponse.StatusEnum.COMPLETED;
            case "paused" -> InternResponse.StatusEnum.PAUSED;
            default -> InternResponse.StatusEnum.ACTIVE;
        };
    }
}
