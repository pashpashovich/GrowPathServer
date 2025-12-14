package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.service.TaskService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteTaskHandler {

    private final TaskService taskService;

    public void handle(DeleteTaskCommand command) {
        taskService.deleteTask(command);
    }
}
