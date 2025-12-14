package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.service.TaskService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateTaskHandler {

    private final TaskService taskService;

    public Task handle(CreateTaskCommand command) {
        return taskService.createTask(command);
    }
}
