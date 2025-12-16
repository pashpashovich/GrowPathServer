package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.TaskListResponse;
import by.bsuir.growpathserver.trainee.application.query.GetInternTasksQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternTasksHandler {

    private final GetTasksHandler getTasksHandler;
    private final TaskMapper taskMapper;

    public TaskListResponse handle(GetInternTasksQuery query) {
        GetTasksQuery getTasksQuery = GetTasksQuery.builder()
                .assignee(query.internId())
                .status(query.status() != null ? TaskStatus.fromString(query.status()) : null)
                .build();

        Page<Task> tasksPage = getTasksHandler.handle(getTasksQuery);

        TaskListResponse response = new TaskListResponse();
        response.setData(tasksPage.getContent().stream()
                                 .map(taskMapper::toTaskResponse)
                                 .toList());

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(tasksPage.getNumber() + 1);
        pagination.setLimit(tasksPage.getSize());
        pagination.setTotal((int) tasksPage.getTotalElements());
        pagination.setTotalPages(tasksPage.getTotalPages());
        response.setPagination(pagination);

        return response;
    }
}
