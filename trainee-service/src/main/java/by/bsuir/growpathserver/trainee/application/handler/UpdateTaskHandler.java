package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.application.service.TaskService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateTaskHandler {

    private final TaskService taskService;

    public Task handle(UpdateTaskCommand command) {
        return taskService.updateTask(command);
    }
}
