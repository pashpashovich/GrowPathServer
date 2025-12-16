package by.bsuir.growpathserver.trainee.application.service;

import by.bsuir.growpathserver.trainee.application.command.CompleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.CreateTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteTaskCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateTaskCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;

public interface TaskService {
    Task createTask(CreateTaskCommand command);

    Task updateTask(UpdateTaskCommand command);

    Task completeTask(CompleteTaskCommand command);

    void deleteTask(DeleteTaskCommand command);

    Task getTaskById(Long id);
}
