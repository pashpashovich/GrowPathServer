package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.service.TaskService;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompleteTaskHandler {

    private final TaskService taskService;

    public Task handle(CompleteTaskCommand command) {
        return taskService.completeTask(command);
    }
}
