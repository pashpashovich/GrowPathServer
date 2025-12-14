package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetTaskByIdQuery;
import by.bsuir.growpathserver.trainee.application.service.TaskService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetTaskByIdHandler {

    private final TaskService taskService;

    public Task handle(GetTaskByIdQuery query) {
        return taskService.getTaskById(query.id());
    }
}
